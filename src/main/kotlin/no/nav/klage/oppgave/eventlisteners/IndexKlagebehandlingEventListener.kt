package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.domain.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.service.IndexService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class IndexKlagebehandlingEventListener(private val indexService: IndexService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    /* Denne kjøres utenfor transaksjonen. Trenger man at dette kjøres i en transaksjon, kan man bruke @Transactional(propagation = Propagation.REQUIRES_NEW)  eller en kombinasjon av @Transactional og @Async */
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun indexKlagebehandling(klagebehandlingEndretEvent: KlagebehandlingEndretEvent) {
        logger.debug("Received KlagebehandlingEndretEvent for klagebehandlingId ${klagebehandlingEndretEvent.klagebehandling.id}")
        indexService.indexKlagebehandling(klagebehandlingEndretEvent.klagebehandling)
    }
}