package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.Saksbehandlertildeling
import no.nav.klage.oppgave.api.view.TildelingEditedView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/ansatte")
class KlagebehandlingAssignmentController(
    private val klagebehandlingService: KlagebehandlingService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Move this to KlagebehandlingController and URL /klagebehandlinger/{id}/saksbehandlerident , equal to medunderskriverident-operations?
    @PostMapping("/{navIdent}/klagebehandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en klagebehandling")
        @PathVariable("id") klagebehandlingId: UUID,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): TildelingEditedView {
        logger.debug("assignSaksbehandler is requested for klagebehandling: {}", klagebehandlingId)
        val klagebehandling = klagebehandlingService.assignKlagebehandling(
            klagebehandlingId,
            saksbehandlertildeling.navIdent,
            saksbehandlertildeling.angittEnhetOrDefault(),
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        return TildelingEditedView(
            klagebehandling.modified,
            klagebehandling.tildeling!!.tidspunkt.toLocalDate()
        )
    }

    @PostMapping("/{navIdent}/klagebehandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en klagebehandling")
        @PathVariable("id") klagebehandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandler is requested for klagebehandling: {}", klagebehandlingId)
        val klagebehandling = klagebehandlingService.assignKlagebehandling(
            klagebehandlingId,
            null,
            null,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )

        return TildelingEditedView(
            klagebehandling.modified,
            klagebehandling.tildeling!!.tidspunkt.toLocalDate()
        )
    }

    // Vi har bestemt at det er greit å hente dette fra db, men jeg beholder muligheten her til å sende det inn fra frontend "just in case".. :)
    private fun Saksbehandlertildeling.angittEnhetOrDefault(): String =
        enhetId ?: saksbehandlerService.findValgtEnhet(innloggetSaksbehandlerRepository.getInnloggetIdent()).enhetId

}

