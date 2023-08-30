package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.MedunderskriverFlowStateResponse
import no.nav.klage.oppgave.api.view.MedunderskriverFlowStateView
import no.nav.klage.oppgave.api.view.MedunderskriverWrapped
import no.nav.klage.oppgave.api.view.SaksbehandlerInput
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logBehandlingMethodDetails
import no.nav.klage.oppgave.util.logKlagebehandlingMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/behandlinger")
class BehandlingMedunderskriverController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val behandlingService: BehandlingService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PutMapping("/{behandlingId}/medunderskriver")
    fun putMedunderskriver(
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: SaksbehandlerInput
    ): MedunderskriverWrapped {
        logBehandlingMethodDetails(
            ::putMedunderskriver.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        return behandlingService.setMedunderskriverIdentAndMedunderskriverFlowState(
            behandlingId,
            input.navIdent,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
    }

    @Operation(
        summary = "Flytter behandlingen mellom saksbehandler og medunderskriver.",
        description = "Flytter fra saksbehandler til medunderskriver dersom saksbehandler utfører, flytter til saksbehandler med returnert-status dersom medunderskriver utfører."
    )
    @PostMapping("/{behandlingId}/send")
    fun switchMedunderskriverFlowState(
        @Parameter(description = "Id til behandlingen i vårt system")
        @PathVariable("behandlingId") behandlingId: UUID
    ): MedunderskriverFlowStateResponse {
        logKlagebehandlingMethodDetails(
            ::switchMedunderskriverFlowState.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        return behandlingService.switchMedunderskriverFlowState(
            behandlingId,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
    }

    @GetMapping("/{behandlingId}/medunderskriver")
    fun getMedunderskriver(
        @PathVariable("behandlingId") behandlingId: UUID
    ): MedunderskriverWrapped {
        logBehandlingMethodDetails(
            ::getMedunderskriver.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        return behandlingService.getMedunderskriver(behandlingId)
    }

    //TODO remove when frontend is updated and using /medunderskriver. Remove when we have events..
    @GetMapping("/{id}/medunderskriverflowstate")
    fun getMedunderskriverFlowState(
        @PathVariable("id") behandlingId: UUID
    ): MedunderskriverFlowStateView {
        logBehandlingMethodDetails(
            ::getMedunderskriverFlowState.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        return behandlingService.getMedunderskriverFlowState(behandlingId)
    }
}