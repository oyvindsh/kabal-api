package no.nav.klage.oppgave.events

import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopiVersjon

data class OppgaveMottattEvent(val oppgavekopiVersjoner: List<OppgaveKopiVersjon>)