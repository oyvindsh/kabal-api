package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.events.OppgaveMottattEvent
import no.nav.klage.oppgave.service.KlagebehandlingService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class OppgaveMottattEventListener(private val klagebehandlingService: KlagebehandlingService) {

    /* Denne kjøres i samme transaksjon som den forrige, det er ingen endring på det selv om det nå brukes events. */
    @EventListener
    fun connectOppgaveToMottak(oppgaveMottattEvent: OppgaveMottattEvent) {
        klagebehandlingService.connectOppgaveKopiToKlagebehandling(oppgaveMottattEvent.oppgavekopiVersjoner)
    }
}