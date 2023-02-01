package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.repositories.MeldingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class CleanupAfterBehandlingEventListener(
    private val meldingRepository: MeldingRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @EventListener
    fun cleanupAfterBehandling(behandlingEndretEvent: BehandlingEndretEvent) {
        if (behandlingEndretEvent.behandling.isAvsluttet()) {
            logger.debug("Received behandlingEndretEvent for avsluttet behandling. Deleting meldinger.")

            meldingRepository.findByBehandlingIdOrderByCreatedDesc(behandlingId = behandlingEndretEvent.behandling.id)
                .forEach { melding ->
                    try {
                        meldingRepository.delete(melding)
                    } catch (exception: Exception) {
                        secureLogger.error("Could not delete melding with id ${melding.id}", exception)
                    }
                }
        }
    }
}