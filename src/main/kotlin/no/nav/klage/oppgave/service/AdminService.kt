package no.nav.klage.oppgave.service

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.dokument.clients.klagefileapi.FileApiClient
import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.clients.skjermede.SkjermedeApiClient
import no.nav.klage.oppgave.domain.kafka.EventType
import no.nav.klage.oppgave.domain.kafka.StatistikkTilDVH
import no.nav.klage.oppgave.domain.kafka.UtsendingStatus
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.repositories.*
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class AdminService(
    private val kafkaDispatcher: KafkaDispatcher,
    private val behandlingRepository: BehandlingRepository,
    private val ankebehandlingRepository: AnkebehandlingRepository,
    private val ankeITrygderettenbehandlingRepository: AnkeITrygderettenbehandlingRepository,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val behandlingEndretKafkaProducer: BehandlingEndretKafkaProducer,
    private val kafkaEventRepository: KafkaEventRepository,
    private val fileApiClient: FileApiClient,
    private val ankeITrygderettenbehandlingService: AnkeITrygderettenbehandlingService,
    private val endringsloggRepository: EndringsloggRepository,
    private val skjermedeApiClient: SkjermedeApiClient
) {

    companion object {
        private const val TWO_SECONDS = 2000L

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    fun syncKafkaWithDb() {
        var pageable: Pageable =
            PageRequest.of(0, 50, Sort.by("created").descending())
        do {
            val behandlingPage = behandlingRepository.findAll(pageable)

            behandlingPage.content.map { behandling ->
                try {
                    when (behandling.type) {
                        Type.KLAGE ->
                            behandlingEndretKafkaProducer.sendKlageEndretV2(behandling as Klagebehandling)

                        Type.ANKE ->
                            behandlingEndretKafkaProducer.sendAnkeEndretV2(behandling as Ankebehandling)

                        Type.ANKE_I_TRYGDERETTEN ->
                            behandlingEndretKafkaProducer.sendAnkeITrygderettenEndretV2(behandling as AnkeITrygderettenbehandling)
                    }
                } catch (e: Exception) {
                    logger.warn("Exception during send to Kafka", e)
                }
            }

            pageable = behandlingPage.nextPageable()
        } while (pageable.isPaged)
    }

    /** only for use in dev */
    fun deleteBehandlingInDev(behandlingId: UUID) {
        logger.debug("Delete test data in dev: attempt to delete behandling with id $behandlingId")
        val dokumenterUnderBehandling = dokumentUnderArbeidRepository.findByBehandlingId(behandlingId)

        for (dub in dokumenterUnderBehandling) {
            try {
                fileApiClient.deleteDocument(id = dub.mellomlagerId!!, systemUser = true)
            } catch (e: Exception) {
                logger.warn("Delete test data in dev: Could not delete from file api")
            }
        }

        dokumentUnderArbeidRepository.deleteAll(dokumenterUnderBehandling)

        endringsloggRepository.deleteAll(endringsloggRepository.findByBehandlingIdOrderByTidspunktDesc(behandlingId))

        behandlingRepository.deleteById(behandlingId)

        //Delete in search
        behandlingEndretKafkaProducer.sendBehandlingDeleted(behandlingId)

        //Delete in dokumentarkiv? Probably not necessary. They clean up when they need to.
    }

    fun resendToDVH() {
        logger.debug("Attempting to resend all events to DVH")
        kafkaDispatcher.dispatchEventsToKafka(
            EventType.STATS_DVH,
            listOf(UtsendingStatus.IKKE_SENDT, UtsendingStatus.FEILET, UtsendingStatus.SENDT)
        )
    }

    fun generateMissingAnkeITrygderetten() {
        logger.debug("Attempting generate missing AnkeITrygderettenBehandling")

        val candidates =
            ankebehandlingRepository.findByDelbehandlingerAvsluttetIsNotNullAndDelbehandlingerUtfallIn(
                utfallToTrygderetten
            )

        val existingAnkeITrygderettenBehandlingKildereferanseAndFagsystem =
            ankeITrygderettenbehandlingRepository.findAll().map { it.kildeReferanse to it.sakFagsystem }

        val ankebehandlingerWithouthAnkeITrygderetten =
            candidates.filter { it.kildeReferanse to it.sakFagsystem !in existingAnkeITrygderettenBehandlingKildereferanseAndFagsystem }

        val ankebehandlingerWithAnkeITrygderetten =
            candidates.filter { it.kildeReferanse to it.sakFagsystem in existingAnkeITrygderettenBehandlingKildereferanseAndFagsystem }

        var logString = ""

        logString += "Antall kandidater blant Ankebehandlinger: ${candidates.size} \n"

        logString += "Antall manglende ankeITrygderetten: ${ankebehandlingerWithouthAnkeITrygderetten.size} \n"
        logString += "Antall tidligere opprettede ankeITrygderetten: ${ankebehandlingerWithAnkeITrygderetten.size} \n\n"

        ankebehandlingerWithouthAnkeITrygderetten.forEach {
            try {
                ankeITrygderettenbehandlingService.createAnkeITrygderettenbehandling(
                    it.createAnkeITrygderettenbehandlingInput()
                )
                logString += "Mangler: ankeBehandlingId: ${it.id},  kildeReferanse: ${it.kildeReferanse} \n"
            } catch (e: Exception) {
                logger.warn(
                    "Klarte ikke å opprette ankeITrygderettenbehandling basert på ankebehandling ${it.id}. Undersøk!",
                    e
                )
            }
        }

        ankebehandlingerWithAnkeITrygderetten.forEach {
            logString += "Finnes fra før: ankeBehandlingId: ${it.id},  kildeReferanse: ${it.kildeReferanse} \n"
        }

        val existingAnkeITrygderettenBehandlingKildereferanseAndFagsystemAfter =
            ankeITrygderettenbehandlingRepository.findAll().map { it.kildeReferanse to it.sakFagsystem }

        val ankebehandlingerWithouthAnkeITrygderettenAfter =
            candidates.filter { it.kildeReferanse to it.sakFagsystem !in existingAnkeITrygderettenBehandlingKildereferanseAndFagsystemAfter }

        val ankebehandlingerWithAnkeITrygderettenAfter =
            candidates.filter { it.kildeReferanse to it.sakFagsystem in existingAnkeITrygderettenBehandlingKildereferanseAndFagsystemAfter }

        logString += "Antall manglende ankeITrygderetten etter operasjonen: ${ankebehandlingerWithouthAnkeITrygderettenAfter.size} \n"
        logString += "Antall opprettede ankeITrygderetten etter operasjonen: ${ankebehandlingerWithAnkeITrygderettenAfter.size} \n"

        logger.debug(logString)
    }

    fun isSkjermet(fnr: String) {
        try {
            logger.debug("isSkjermet called")
            val isSkjermet = skjermedeApiClient.isSkjermet(fnr)

            secureLogger.debug("isSkjermet: {} for fnr {}", isSkjermet, fnr)
        } catch (e: Exception) {
            secureLogger.error("isSkjermet failed for fnr $fnr", e)
        }
    }

    fun migrateDvhEvents() {
        val events = kafkaEventRepository.findAllById(
            eventsIdsAsStrings.map { UUID.fromString(it) }
        )
        logger.debug("Number of candidates: ${events.size}")

        events.forEach {
            logger.debug("BEFORE: Modifying kafka event ${it.id}, behandling_id ${it.behandlingId}, payload: ${it.jsonPayload}")
            var parsedStatistikkTilDVH = objectMapper.readValue(it.jsonPayload, StatistikkTilDVH::class.java)
            parsedStatistikkTilDVH = parsedStatistikkTilDVH.copy(tekniskTid = LocalDateTime.now())

            it.jsonPayload = objectMapper.writeValueAsString(parsedStatistikkTilDVH)
            it.status = UtsendingStatus.IKKE_SENDT
            logger.debug("AFTER: Modified kafka event ${it.id}, behandling_id ${it.behandlingId}, payload: ${it.jsonPayload}")
        }
    }
}

val eventsIdsAsStrings = listOf(
    "6928c71e-1172-495d-9f0d-752f6b11f8ca",
    "28273104-d5c1-41ff-906b-364ad45f1b75",
    "715168e9-aa19-4daa-8125-db959c6a0eb7",
    "7f41e9f5-579b-41c0-88d5-10c09d8223f9",
    "caf04a29-9559-4dd4-97f7-adce46022549",
    "968c3d84-f72c-40c4-bc81-6222f86b5508",
    "9f848bbc-8399-4986-a070-77147017a0bd",
    "217282d8-b801-428b-a217-d2e57804751b",
    "5553404b-ebdb-44af-a788-15a15dde9440",
    "24e3c233-dc19-4f3c-9f77-464c9c936766",
    "da52653c-1951-49ce-82d8-614b51cf7f53",
    "c457e808-ac87-4839-b4fe-18f6057d1c03",
    "d0d42df8-8239-408e-80b5-e51b7cd38c78",
    "d28adf6b-8330-4f06-9502-4ee7737abd9d",
    "701ce190-b325-4177-8744-3e8b5f2a186a",
    "4caa6196-4f71-480e-943e-883e0026ddf5",
    "9a1041b1-5eb2-408a-bd26-e905e3d6cd61",
    "1f8841e8-9fb9-45e0-b8d3-34aa7d4da4da",
    "98b673d8-6621-409a-b46e-716cd9d69270",
    "452bf8be-e8d7-4034-98af-1e63724df4a0",
    "fb88c206-65fb-46b9-9d56-440523688829",
    "4f1b5d7e-f76b-4aaa-aae0-3a7e819c2e8f",
    "0cd39a5a-dacd-4d15-a282-b06193698025",
    "3bb55325-a54f-43de-894c-fbb10e46d162",
    "b4cab8e6-cb31-465e-960d-9c7e464a79d1",
    "499807a3-e229-4a22-8701-7f0e6dfcd111",
    "5d54e647-f7db-46f9-9ca6-0119808b2b59",
    "fac36137-fccd-406b-9885-0d65641e85db",
    "793c21b6-0f99-47db-b787-cefe39bb8574",
    "11328156-4841-419d-86d9-ce123be261c9",
    "5ba8e028-057a-42e9-82c8-cb3b83842fa3",
    "568cce39-f450-4772-a61f-6bc06cbbf34d",
    "baf6dee4-9d08-48d7-8608-fe2780bccb1e",
    "967f044e-8fac-45bf-80f2-0712749f18fb",
    "9d0eec01-ffd7-4993-a427-c72503381159",
    "93706cfb-2de9-4988-a2d8-1812319bd00c",
    "d22f05fd-0ae9-4b44-95ba-9eb52a3584a6",
    "efa5530a-1408-4eab-ba58-fe2fbba2b426",
    "5f6baf29-b6b3-4cfe-8aa5-ff349fac8927",
    "8160158d-d157-4174-a328-10fe68ed2029",
    "fbcfcb9e-1f0d-4ada-a343-506e414b670a",
    "7aeae9e3-25c9-4c33-8cff-1ad5f1f23079",
    "9c668223-96e6-4f99-8f4f-ee516a23c085",
    "8be172fd-3479-4d93-a20e-e52ea36931e0",
    "d783277a-0221-4b3f-ab61-e9579b7f093c",
    "7ff514df-fdba-4029-bb70-2c34ed6690d8",
    "bf69b180-6cf9-4b9a-8f6b-7e5e5c80e36b",
)