package no.nav.klage.oppgave.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.transaction.Transactional
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.kafka.*
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandling
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandlingInput
import no.nav.klage.oppgave.domain.klage.Delbehandling
import no.nav.klage.oppgave.repositories.AnkeITrygderettenbehandlingRepository
import no.nav.klage.oppgave.repositories.KafkaEventRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class AnkeITrygderettenbehandlingService(
    private val ankeITrygderettenbehandlingRepository: AnkeITrygderettenbehandlingRepository,
    private val vedtakService: VedtakService,
    private val behandlingService: BehandlingService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val mottakService: MottakService,
    private val dokumentService: DokumentService,
    private val kafkaEventRepository: KafkaEventRepository,
    @Value("#{T(java.time.LocalDate).parse('\${KAKA_VERSION_2_DATE}')}")
    private val kakaVersion2Date: LocalDate,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val SYSTEMBRUKER = "SYSTEMBRUKER"
        private val objectMapperBehandlingEvents = ObjectMapper().registerModule(JavaTimeModule()).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        )
    }

    private fun getKakaVersion(): Int {
        val kvalitetsvurderingVersion = if (LocalDate.now() >= kakaVersion2Date) {
            2
        } else {
            1
        }
        return kvalitetsvurderingVersion
    }

    fun createAnkeITrygderettenbehandling(input: AnkeITrygderettenbehandlingInput): AnkeITrygderettenbehandling {
        val ankeITrygderettenbehandling = ankeITrygderettenbehandlingRepository.save(
            AnkeITrygderettenbehandling(
                klager = input.klager.copy(),
                sakenGjelder = input.sakenGjelder?.copy() ?: input.klager.toSakenGjelder(),
                ytelse = input.ytelse,
                type = input.type,
                kildeReferanse = input.kildeReferanse,
                dvhReferanse = input.dvhReferanse,
                sakFagsystem = input.sakFagsystem,
                sakFagsakId = input.sakFagsakId,
                mottattKlageinstans = input.sakMottattKlageinstans,
                tildeling = null,
                delbehandlinger = setOf(Delbehandling()),
                hjemler = if (input.innsendingsHjemler.isNullOrEmpty()) {
                    mutableSetOf(Hjemmel.MANGLER)
                } else {
                    input.innsendingsHjemler
                },
                sendtTilTrygderetten = input.sendtTilTrygderetten,
                kjennelseMottatt = null,
                kakaKvalitetsvurderingVersion = getKakaVersion()
            )
        )
        logger.debug("Created ankeITrygderettenbehandling ${ankeITrygderettenbehandling.id}")

        if (input.registreringsHjemmelSet != null) {
            vedtakService.setHjemler(
                behandlingId = ankeITrygderettenbehandling.id,
                hjemler = input.registreringsHjemmelSet,
                utfoerendeSaksbehandlerIdent = SYSTEMBRUKER,
                systemUserContext = true,
            )
        }

        input.saksdokumenter.forEach {
            behandlingService.connectDokumentToBehandling(
                behandlingId = ankeITrygderettenbehandling.id,
                journalpostId = it.journalpostId,
                dokumentInfoId = it.dokumentInfoId,
                saksbehandlerIdent = SYSTEMBRUKER,
                systemUserContext = true,
            )
        }

        applicationEventPublisher.publishEvent(
            BehandlingEndretEvent(
                behandling = ankeITrygderettenbehandling,
                endringslogginnslag = emptyList()
            )
        )

        //Publiser Kafka-event, infomelding om opprettelse
        val behandlingEvent = BehandlingEvent(
            eventId = UUID.randomUUID(),
            kildeReferanse = ankeITrygderettenbehandling.kildeReferanse,
            kilde = ankeITrygderettenbehandling.sakFagsystem.navn,
            kabalReferanse = ankeITrygderettenbehandling.currentDelbehandling().id.toString(),
            type = BehandlingEventType.ANKE_I_TRYGDERETTENBEHANDLING_OPPRETTET,
            detaljer = BehandlingDetaljer(
                ankeITrygderettenbehandlingOpprettet =
                AnkeITrygderettenbehandlingOpprettetDetaljer(
                    sendtTilTrygderetten = ankeITrygderettenbehandling.sendtTilTrygderetten,
                    utfall = input.ankebehandlingUtfall,
                )
            )
        )
        kafkaEventRepository.save(
            KafkaEvent(
                id = UUID.randomUUID(),
                behandlingId = ankeITrygderettenbehandling.id,
                kilde = ankeITrygderettenbehandling.sakFagsystem.navn,
                kildeReferanse = ankeITrygderettenbehandling.kildeReferanse,
                jsonPayload = objectMapperBehandlingEvents.writeValueAsString(behandlingEvent),
                type = EventType.BEHANDLING_EVENT
            )
        )

        return ankeITrygderettenbehandling
    }
}