package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class BehandlingAssignmentController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val saksbehandlerService: SaksbehandlerService,
    private val behandlingService: BehandlingService,
    private val pdlFacade: PdlFacade,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO remove when FE migrated to new endpoint without navident
    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandlerOld(
        @Parameter(description = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): TildelingEditedView {
        logger.debug("assignSaksbehandlerOld is requested for behandling: {}", behandlingId)

        val behandling = behandlingService.setSaksbehandler(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = saksbehandlertildeling.navIdent,
            enhetId = saksbehandlerService.getEnhetForSaksbehandler(saksbehandlertildeling.navIdent).enhetId,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return TildelingEditedView(
            behandling.modified,
            behandling.tildeling!!.tidspunkt.toLocalDate()
        )
    }

    @PutMapping("/behandlinger/{id}/saksbehandler")
    fun setSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
        @RequestBody saksbehandlerInput: SaksbehandlerInput
    ): SaksbehandlerModifiedResponse {
        logger.debug("setSaksbehandler is requested for behandling: {}", behandlingId)

        val behandling = behandlingService.setSaksbehandler(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = saksbehandlerInput.navIdent,
            enhetId = if (saksbehandlerInput.navIdent != null) saksbehandlerService.getEnhetForSaksbehandler(
                saksbehandlerInput.navIdent
            ).enhetId else null,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(behandling = behandling)
    }

    @GetMapping("/behandlinger/{id}/saksbehandler")
    fun getSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
    ): SaksbehandlerView? {
        logger.debug("getSaksbehandler is requested for behandling: {}", behandlingId)

        val behandling = behandlingService.getBehandling(behandlingId)

        return if (behandling.tildeling?.saksbehandlerident == null) {
            null
        } else {
            SaksbehandlerView(
                navIdent = behandling.tildeling?.saksbehandlerident!!,
                navn = saksbehandlerService.getNameForIdent(behandling.tildeling?.saksbehandlerident!!),
            )
        }
    }

    //TODO remove when FE migrated to new endpoint without navident
    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandlerOld(
        @Parameter(description = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandlerOld is requested for behandling: {}", behandlingId)

        val behandling = behandlingService.setSaksbehandler(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = null,
            enhetId = null,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return TildelingEditedView(
            behandling.modified,
            behandling.tildeling!!.tidspunkt.toLocalDate()
        )
    }

    private fun tildelingEditedView(behandling: Behandling): SaksbehandlerModifiedResponse =
        SaksbehandlerModifiedResponse(
            navIdent = behandling.tildeling?.saksbehandlerident,
            navn = if (behandling.tildeling?.saksbehandlerident != null) saksbehandlerService.getNameForIdent(behandling.tildeling?.saksbehandlerident!!) else null,
            modified = behandling.modified,
        )
}