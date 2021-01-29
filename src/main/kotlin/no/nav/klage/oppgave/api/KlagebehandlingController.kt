package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}")
    fun getKlagebehandling(
        @PathVariable("id") oppgaveId: Long
    ): KlagebehandlingView {
        logger.debug(
            "getKlagebehandling is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            oppgaveId
        )
        return klagebehandlingFacade.getKlagebehandling(oppgaveId)
    }

}