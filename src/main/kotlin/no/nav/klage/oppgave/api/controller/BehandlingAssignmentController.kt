package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.Saksbehandlertildeling
import no.nav.klage.oppgave.api.view.TildelingEditedView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class BehandlingAssignmentController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val saksbehandlerService: SaksbehandlerService,
    private val behandlingService: BehandlingService
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
        val behandling = behandlingService.assignBehandling(
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

    @PostMapping("/behandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): TildelingEditedView {
        logger.debug("assignSaksbehandler is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
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

    //TODO remove when FE migrated to new endpoint without navident
    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandlerOld(
        @Parameter(description = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandlerOld is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
            behandlingId,
            null,
            null,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return TildelingEditedView(
            behandling.modified,
            behandling.tildeling!!.tidspunkt.toLocalDate()
        )
    }

    @PostMapping("/behandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandler is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
            behandlingId,
            null,
            null,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return TildelingEditedView(
            behandling.modified,
            behandling.tildeling!!.tidspunkt.toLocalDate()
        )
    }
}

