package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.domain.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class StatistikkTilDVHEventListener(private val statistikkTilDVHService: StatistikkTilDVHService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }
    
    @EventListener
    fun klagebehandlingEndretEventToDVH(klagebehandlingEndretEvent: KlagebehandlingEndretEvent) {
        logger.debug("Received KlagebehandlingEndretEvent for klagebehandlingId ${klagebehandlingEndretEvent.klagebehandling.id} in StatistikkTilDVHEventListener")
        statistikkTilDVHService.process(klagebehandlingEndretEvent)
    }
}