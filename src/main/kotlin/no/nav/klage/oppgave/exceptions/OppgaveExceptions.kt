package no.nav.klage.oppgave.exceptions

class OppgaveNotFoundException(msg: String) : RuntimeException(msg)

open class ValidationException(msg: String) : RuntimeException(msg)

class OppgaveIdWrongFormatException(msg: String) : ValidationException(msg)

class OppgaveVersjonWrongFormatException(msg: String) : ValidationException(msg)

class BehandlingsidWrongFormatException(msg: String) : ValidationException(msg)

class NotMatchingUserException(msg: String) : RuntimeException(msg)

class FeatureNotEnabledException(msg: String) : RuntimeException(msg)

class NoSaksbehandlerRoleException(msg: String) : RuntimeException(msg)

class NotOwnEnhetException(msg: String) : RuntimeException(msg)
