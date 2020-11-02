package no.nav.klage.oppgave.exceptions

class OppgaveNotFoundException(msg: String) : RuntimeException(msg)

class OppgaveIdWrongFormatException(msg: String) : RuntimeException(msg)