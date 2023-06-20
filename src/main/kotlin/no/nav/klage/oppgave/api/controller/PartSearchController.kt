package no.nav.klage.oppgave.api.controller


import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.FullmektigSearchService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class PartSearchController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val fullmektigSearchService: FullmektigSearchService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/searchfullmektig")
    @Deprecated("Use /searchpart")
    fun searchFullmektig(
        @RequestBody input: IdentifikatorInput,
    ): BehandlingDetaljerView.PartView {
        logMethodDetails(
            ::searchFullmektig.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return fullmektigSearchService.searchFullmektig(input.identifikator)
    }

    @PostMapping("/searchpart")
    fun searchPart(
        @RequestBody input: IdentifikatorInput,
    ): BehandlingDetaljerView.PartView {
        logMethodDetails(
            ::searchPart.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return fullmektigSearchService.searchFullmektig(input.identifikator)
    }

    @PostMapping("/searchperson")
    fun searchPerson(
        @RequestBody input: IdentifikatorInput,
    ): BehandlingDetaljerView.SakenGjelderView {
        logMethodDetails(
            ::searchPerson.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return fullmektigSearchService.searchPerson(input.identifikator)
    }
}