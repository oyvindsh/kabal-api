package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.mapper.BehandlingMapper
import no.nav.klage.oppgave.api.view.*
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
@RequestMapping("/klagebehandlinger")
class BehandlingMedunderskriverController(
    private val behandlingMapper: BehandlingMapper,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val behandlingService: BehandlingService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PutMapping("/{id}/medunderskriverident")
    fun putMedunderskriverident(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: BehandlingMedunderskriveridentInput
    ): MedunderskriverFlytResponse {
        logBehandlingMethodDetails(
            ::putMedunderskriverident.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val behandling = behandlingService.setMedunderskriverIdentAndMedunderskriverFlyt(
            behandlingId,
            input.medunderskriverident,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return behandlingMapper.mapToMedunderskriverFlytResponse(behandling)
    }

    @PutMapping("/{id}/medunderskriver")
    fun putMedunderskriver(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: SaksbehandlerInput
    ): MedunderskriverFlytResponse {
        logBehandlingMethodDetails(
            ::putMedunderskriverident.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val behandling = behandlingService.setMedunderskriverIdentAndMedunderskriverFlyt(
            behandlingId,
            input.navIdent,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return behandlingMapper.mapToMedunderskriverFlytResponse(behandling)
    }

    @Operation(
        summary = "Flytter behandlingen mellom saksbehandler og medunderskriver.",
        description = "Flytter fra saksbehandler til medunderskriver dersom saksbehandler utfører, flytter til saksbehandler med returnert-status dersom medunderskriver utfører."
    )
    @PostMapping("/{id}/send")
    fun switchMedunderskriverFlyt(
        @Parameter(description = "Id til behandlingen i vårt system")
        @PathVariable("id") behandlingId: UUID
    ): MedunderskriverFlytResponse {
        logKlagebehandlingMethodDetails(
            ::switchMedunderskriverFlyt.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val behandling = behandlingService.switchMedunderskriverFlyt(
            behandlingId,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return behandlingMapper.mapToMedunderskriverFlytResponse(behandling)
    }

    @GetMapping("/{id}/medunderskriver")
    fun getMedunderskriver(
        @PathVariable("id") behandlingId: UUID
    ): SaksbehandlerView? {
        logBehandlingMethodDetails(
            ::getMedunderskriver.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val behandling = behandlingService.getBehandling(behandlingId)
        return behandlingMapper.mapToSaksbehandlerView(behandling)
    }

    @GetMapping("/{id}/medunderskriverflyt")
    fun getMedunderskriverFlyt(
        @PathVariable("id") behandlingId: UUID
    ): MedunderskriverFlytView {
        logBehandlingMethodDetails(
            ::getMedunderskriverFlyt.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val behandling = behandlingService.getBehandling(behandlingId)
        return behandlingMapper.mapToMedunderskriverFlytView(behandling)
    }
}