package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.events.OppgaveMottattEvent
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class ConnectOppgaveToMottakEventListener(private val klagebehandlingService: KlagebehandlingService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    /* Denne kjøres i samme transaksjon som den forrige, det er ingen endring på det selv om det nå brukes events. */
    @EventListener
    fun connectOppgaveToMottak(oppgaveMottattEvent: OppgaveMottattEvent) {
        logger.info("Received OppgaveMottattEvent for oppgaveId ${oppgaveMottattEvent.oppgavekopiVersjoner.firstOrNull()?.id}")
        klagebehandlingService.connectOppgaveKopiToKlagebehandling(oppgaveMottattEvent.oppgavekopiVersjoner)
    }
}