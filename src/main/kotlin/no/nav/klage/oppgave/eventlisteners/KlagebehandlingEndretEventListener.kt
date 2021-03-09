package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.service.IndexService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class KlagebehandlingEndretEventListener(private val indexService: IndexService) {

    /* Denne kjøres utenfor transaksjonen. Trenger man at dette kjøres i en transaksjon, kan man bruke @Transactional(propagation = Propagation.REQUIRES_NEW)  eller en kombinasjon av @Transactional og @Async */
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun indexKlagebehandling(klagebehandlingEndretEvent: KlagebehandlingEndretEvent) {
        indexService.indexKlagebehandling(klagebehandlingEndretEvent.klagebehandling)
    }
}