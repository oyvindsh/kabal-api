package no.nav.klage.oppgave.exceptions

class OppgaveNotFoundException(msg: String) : RuntimeException(msg)

class OppgaveIdWrongFormatException(msg: String) : RuntimeException(msg)

class OppgaveVersjonWrongFormatException(msg: String) : RuntimeException(msg)

class NotMatchingUserException(msg: String) : RuntimeException(msg)

class FeatureNotEnabledException(msg: String) : RuntimeException(msg)