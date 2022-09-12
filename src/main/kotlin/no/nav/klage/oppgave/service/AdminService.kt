package no.nav.klage.oppgave.service

import no.nav.klage.dokument.clients.klagefileapi.FileApiClient
import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.domain.kafka.EventType
import no.nav.klage.oppgave.domain.kafka.UtsendingStatus
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.repositories.AnkeITrygderettenbehandlingRepository
import no.nav.klage.oppgave.repositories.AnkebehandlingRepository
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.repositories.EndringsloggRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdminService(
    private val kafkaDispatcher: KafkaDispatcher,
    private val behandlingRepository: BehandlingRepository,
    private val ankebehandlingRepository: AnkebehandlingRepository,
    private val ankeITrygderettenbehandlingRepository: AnkeITrygderettenbehandlingRepository,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val behandlingEndretKafkaProducer: BehandlingEndretKafkaProducer,
    private val fileApiClient: FileApiClient,
    private val ankeITrygderettenbehandlingService: AnkeITrygderettenbehandlingService,
    private val endringsloggRepository: EndringsloggRepository,
) {

    companion object {
        private const val TWO_SECONDS = 2000L

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
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
        logString += "Antall opprettede ankeITrygderetten: ${ankebehandlingerWithAnkeITrygderetten.size} \n\n"

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
}