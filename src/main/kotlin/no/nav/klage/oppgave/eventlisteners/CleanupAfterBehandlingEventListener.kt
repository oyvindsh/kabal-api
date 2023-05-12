package no.nav.klage.oppgave.eventlisteners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.klage.dokument.service.DokumentUnderArbeidService
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.clients.kaka.KakaApiGateway
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.kafka.*
import no.nav.klage.oppgave.domain.klage.Ankebehandling
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.repositories.KafkaEventRepository
import no.nav.klage.oppgave.repositories.MeldingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class CleanupAfterBehandlingEventListener(
    private val meldingRepository: MeldingRepository,
    private val kafkaEventRepository: KafkaEventRepository,
    private val kakaApiGateway: KakaApiGateway,
    private val dokumentUnderArbeidService: DokumentUnderArbeidService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapperBehandlingEvents = ObjectMapper().registerModule(JavaTimeModule()).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        )
    }

    @EventListener
    fun cleanupAfterBehandling(behandlingEndretEvent: BehandlingEndretEvent) {
        val behandling = behandlingEndretEvent.behandling
        if (behandling.isAvsluttet()) {
            logger.debug("Received behandlingEndretEvent for avsluttet behandling. Deleting meldinger.")

            meldingRepository.findByBehandlingIdOrderByCreatedDesc(behandlingId = behandling.id)
                .forEach { melding ->
                    try {
                        meldingRepository.delete(melding)
                    } catch (exception: Exception) {
                        secureLogger.error("Could not delete melding with id ${melding.id}", exception)
                    }
                }
        } else if (behandling.feilregistrering != null) {
            logger.debug("Cleanup and notifying vedtaksinstans after feilregistrering. Behandling.id: {}", behandling.id)
            deleteDokumenterUnderBehandling(behandling)
            deleteFromKaka(behandling)
            notifyVedtaksinstans(behandling)
        }
    }

    private fun deleteDokumenterUnderBehandling(behandling: Behandling) {
        dokumentUnderArbeidService.findDokumenterNotFinished(behandlingId = behandling.id).forEach {
            try {
                dokumentUnderArbeidService.slettDokument(
                    behandlingId = behandling.id,
                    dokumentId = it.id,
                    innloggetIdent = behandling.feilregistrering!!.navIdent
                )
            } catch (e: Exception) {
                //best effort
                logger.warn("Couldn't clean up dokumenter under arbeid")
            }
        }
    }

    private fun notifyVedtaksinstans(behandling: Behandling) {
        val behandlingEvent = BehandlingEvent(
            eventId = UUID.randomUUID(),
            kildeReferanse = behandling.kildeReferanse,
            kilde = behandling.fagsystem.navn,
            kabalReferanse = behandling.id.toString(),
            type = BehandlingEventType.BEHANDLING_FEILREGISTRERT,
            detaljer = BehandlingDetaljer(
                behandlingFeilregistrert =
                BehandlingFeilregistrertDetaljer(
                    navIdent = behandling.feilregistrering!!.navIdent,
                    reason = behandling.feilregistrering!!.reason,
                )
            )
        )
        kafkaEventRepository.save(
            KafkaEvent(
                id = UUID.randomUUID(),
                behandlingId = behandling.id,
                kilde = behandling.fagsystem.navn,
                kildeReferanse = behandling.kildeReferanse,
                jsonPayload = objectMapperBehandlingEvents.writeValueAsString(behandlingEvent),
                type = EventType.BEHANDLING_EVENT
            )
        )
    }

    private fun deleteFromKaka(behandling: Behandling) {
        when (behandling.type) {
            Type.KLAGE -> {
                behandling as Klagebehandling
                when (behandling.kakaKvalitetsvurderingVersion) {
                    2 -> {
                        kakaApiGateway.deleteKvalitetsvurderingV2(behandling.kakaKvalitetsvurderingId)
                    }
                }
            }

            Type.ANKE -> {
                behandling as Ankebehandling
                when (behandling.kakaKvalitetsvurderingVersion) {
                    2 -> {
                        kakaApiGateway.deleteKvalitetsvurderingV2(behandling.kakaKvalitetsvurderingId)
                    }
                }
            }

            Type.ANKE_I_TRYGDERETTEN -> {}//nothing
        }
    }
}