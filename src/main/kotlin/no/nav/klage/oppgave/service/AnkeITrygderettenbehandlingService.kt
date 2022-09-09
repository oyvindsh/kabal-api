package no.nav.klage.oppgave.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.api.view.OversendtAnkeITrygderettenV1
import no.nav.klage.oppgave.api.view.createAnkeITrygderettenbehandlingInput
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.kafka.*
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandling
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandlingInput
import no.nav.klage.oppgave.domain.klage.Delbehandling
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.repositories.AnkeITrygderettenbehandlingRepository
import no.nav.klage.oppgave.repositories.KafkaEventRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.*
import javax.transaction.Transactional

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
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val SYSTEMBRUKER = "SYSTEMBRUKER"
        private val objectMapperBehandlingEvents = ObjectMapper().registerModule(JavaTimeModule()).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        )
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
                hjemler = if (input.innsendingsHjemler == null || input.innsendingsHjemler.isEmpty()) {
                    mutableSetOf(Hjemmel.MANGLER)
                } else {
                    input.innsendingsHjemler
                },
                sendtTilTrygderetten = input.sendtTilTrygderetten,
                kjennelseMottatt = null,
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
            try {
                behandlingService.connectDokumentToBehandling(
                    behandlingId = ankeITrygderettenbehandling.id,
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId,
                    saksbehandlerIdent = SYSTEMBRUKER,
                    systemUserContext = true,
                )
            } catch (exception: JournalpostNotFoundException) {
                logger.warn("Journalpost with id ${it.journalpostId} not found. Skipping document..")
            }

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
            type = BehandlingEventType.ANKE_I_TRYGDERETTEN_OPPRETTET,
            detaljer = BehandlingDetaljer(
                ankeITrygderettenbehandlingOpprettetDetaljer =
                AnkeITrygderettenbehandlingOpprettetDetaljer(opprettet = ankeITrygderettenbehandling.created)
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

    fun createAnkeITrygderettenbehandling(input: OversendtAnkeITrygderettenV1) {
        mottakService.validateAnkeITrygderettenV1(input)
        val inputDocuments =
            dokumentService.createSaksdokumenterFromJournalpostIdSet(input.tilknyttedeJournalposter.map { it.journalpostId })
        createAnkeITrygderettenbehandling(
            input.createAnkeITrygderettenbehandlingInput(inputDocuments)
        )
    }
}