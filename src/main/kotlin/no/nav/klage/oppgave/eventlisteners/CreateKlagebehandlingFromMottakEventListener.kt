package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.events.MottakLagretEvent
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class CreateKlagebehandlingFromMottakEventListener(private val klagebehandlingService: KlagebehandlingService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @EventListener
    fun createKlagebehandling(mottakLagretEvent: MottakLagretEvent) {
        logger.debug("Received MottakLagretEvent for mottak ${mottakLagretEvent.mottak.id} in CreateKlagebehandlingFromMottakEventListener")
        klagebehandlingService.createKlagebehandlingFromMottak(mottakLagretEvent.mottak)
    }
}