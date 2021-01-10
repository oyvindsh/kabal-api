package no.nav.klage.oppgave.api.internal

import no.nav.klage.oppgave.api.OppgaveFacade
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "aad")
@RequestMapping("internal")
class InternalController(private val oppgaveFacade: OppgaveFacade) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @PostMapping("/oppgaver")
    fun saveOppgave(@RequestBody oppgave: OppgaveKopiAPIModel) {
        logger.debug("saveOppgave is requested. OppgaveId: {}", oppgave.id)
        secureLogger.debug("saveOppgave is requested. Oppgave: {}", oppgave)
        oppgaveFacade.saveOppgaveKopi(oppgave)
    }

}