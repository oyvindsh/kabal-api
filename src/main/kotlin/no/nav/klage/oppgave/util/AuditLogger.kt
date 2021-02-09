package no.nav.klage.oppgave.util

import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.domain.AuditLogEvent.Level.INFO
import no.nav.klage.oppgave.domain.AuditLogEvent.Level.WARN
import org.springframework.stereotype.Component
import java.lang.String.join

@Component
class AuditLogger {

    companion object {
        val auditLogger = getAuditLogger()
    }

    fun log(logEvent: AuditLogEvent) {
        when (logEvent.logLevel) {
            WARN -> {
                auditLogger.warn(compileLogMessage(logEvent))
            }
            INFO -> {
                auditLogger.info(compileLogMessage(logEvent))
            }
        }
    }

    private fun compileLogMessage(logEvent: AuditLogEvent): String {
        val version = "CEF:0"
        val deviceVendor = logEvent.applicationName
        val deviceProduct = "auditLog"
        val deviceVersion = "1.0"
        val deviceEventClassId = "${logEvent.applicationName}:accessed"
        val name = logEvent.appDescription
        val severity = logEvent.logLevel.name

        val extensions = join(" ", getExtensions(logEvent))

        return join(
            "|", listOf(
                version,
                deviceVendor,
                deviceProduct,
                deviceVersion,
                deviceEventClassId,
                name,
                severity,
                extensions
            )
        )
    }

    private fun getExtensions(logEvent: AuditLogEvent): List<String> =
        listOf(
            "end=${System.currentTimeMillis()}",
            "suid=${logEvent.navIdent}",
            "duid=${logEvent.personFnr}",
            "request=${logEvent.requestURL}",
            "requestMethod=${logEvent.requestMethod}",
            "flexString1=Permit",
            "flexString1Label=Decision",
            "sproc=${logEvent.traceId}}",
        )

}