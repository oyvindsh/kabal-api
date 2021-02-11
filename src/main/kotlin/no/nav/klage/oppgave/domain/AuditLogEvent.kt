package no.nav.klage.oppgave.domain

data class AuditLogEvent(
    val navIdent: String,
    val action: Action,
    val decision: Decision,
    val personFnr: String?,
    val logLevel: Level = Level.INFO
) {
    enum class Level {
        INFO, WARN
    }

    enum class Action(val value: String) {
        KLAGEBEHANDLING_VIEW("klagebehandling:view")
    }

    enum class Decision {
        ALLOW, DENY
    }

}