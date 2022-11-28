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

        val behandlingBeforeChange = behandlingService.getBehandling(behandlingId)

        val behandlingAfterChange = behandlingService.assignBehandling(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = saksbehandlertildeling.navIdent,
            enhetId = saksbehandlerService.getEnhetForSaksbehandler(saksbehandlertildeling.navIdent).enhetId,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(fromBehandling = behandlingBeforeChange, toBehandling = behandlingAfterChange)
    }

    @PostMapping("/behandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): TildelingEditedView {
        logger.debug("assignSaksbehandler is requested for behandling: {}", behandlingId)

        val behandlingBeforeChange = behandlingService.getBehandling(behandlingId)

        val behandlingAfterChange = behandlingService.assignBehandling(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = saksbehandlertildeling.navIdent,
            enhetId = saksbehandlerService.getEnhetForSaksbehandler(saksbehandlertildeling.navIdent).enhetId,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(fromBehandling = behandlingBeforeChange, toBehandling = behandlingAfterChange)
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

        val behandlingBeforeChange = behandlingService.getBehandling(behandlingId)

        val behandlingAfterChange = behandlingService.assignBehandling(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = null,
            enhetId = null,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(fromBehandling = behandlingBeforeChange, toBehandling = behandlingAfterChange)
    }

    @PostMapping("/behandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandler is requested for behandling: {}", behandlingId)

        val behandlingBeforeChange = behandlingService.getBehandling(behandlingId)

        val behandlingAfterChange = behandlingService.assignBehandling(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = null,
            enhetId = null,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(fromBehandling = behandlingBeforeChange, toBehandling = behandlingAfterChange)
    }

    private fun tildelingEditedView(fromBehandling: Behandling, toBehandling: Behandling): TildelingEditedView {
        val fnr = fromBehandling.sakenGjelder.partId.value
        val personInfo = pdlFacade.getPersonInfo(fnr)

        val tildelingEditedView = TildelingEditedView(
            person = TildelingEditedView.PersonView(
                fnr = fnr,
                navn = personInfo.sammensattNavn,
            ),
            fromSaksbehandler = fromBehandling.tildeling?.let {
                SaksbehandlerView(
                    navIdent = it.saksbehandlerident!!,
                    navn = saksbehandlerService.getNameForIdent(it.saksbehandlerident)
                )
            },
            toSaksbehandler = toBehandling.tildeling?.let {
                SaksbehandlerView(
                    navIdent = it.saksbehandlerident!!,
                    navn = saksbehandlerService.getNameForIdent(it.saksbehandlerident)
                )
            },
        )
        return tildelingEditedView
    }
}