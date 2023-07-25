package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.service.CleanupHjelpemidlerService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class CleanupHjelpemidlerController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val cleanupHjelpemidlerService: CleanupHjelpemidlerService,
) {

    companion object {
        private val secureLogger = getSecureLogger()
    }

    @GetMapping("/internal/log-wrong-tema", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun logWrongTemaForHjelpemidler() {

        if (!innloggetSaksbehandlerService.isKabalOppgavestyringAlleEnheter()) {
            throw MissingTilgangException("User does not have the role OppgavestyringAlleEnheter")
        }

        try {
            secureLogger.debug("Log documents that have wrong tema in regards to HJE")
            cleanupHjelpemidlerService.logJournalpostsWeNeedToPatch()
        } catch (e: Exception) {
            secureLogger.warn("Failed to log documents that have wrong tema in regards to HJE", e)
            throw e
        }
    }

}