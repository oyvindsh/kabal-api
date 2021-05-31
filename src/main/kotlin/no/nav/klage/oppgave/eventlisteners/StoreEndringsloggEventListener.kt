package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.domain.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.repositories.EndringsloggRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class StoreEndringsloggEventListener(private val endringsloggRepository: EndringsloggRepository) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @EventListener
    fun storeEndringslogg(klagebehandlingEndretEvent: KlagebehandlingEndretEvent) {
        logger.debug("Received KlagebehandlingEndretEvent for klagebehandlingId ${klagebehandlingEndretEvent.klagebehandling.id} in StoreEndringsloggEventListener")
        if (klagebehandlingEndretEvent.endringslogginnslag.isNotEmpty()) {
            endringsloggRepository.saveAll(klagebehandlingEndretEvent.endringslogginnslag)
        }
    }
}