package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.BehandlingMapper
import no.nav.klage.oppgave.api.view.BehandlingEditedView
import no.nav.klage.oppgave.api.view.BehandlingFullfoertView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logBehandlingMethodDetails
import no.nav.klage.oppgave.util.logKlagebehandlingMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/klagebehandlinger")
class BehandlingController(
    private val behandlingService: BehandlingService,
    private val behandlingMapper: BehandlingMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/{behandlingId}/sattpaavent")
    fun setSattPaaVent(
        @ApiParam(value = "Id til en behandling")
        @PathVariable("behandlingId") behandlingId: UUID,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setSattPaaVent.name,
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val modified = behandlingService.setSattPaaVent(
            behandlingId = behandlingId,
            setNull = false,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        return BehandlingEditedView(modified = modified)
    }

    @DeleteMapping("/{behandlingId}/sattpaavent")
    fun deleteSattPaaVent(
        @ApiParam(value = "Id til en behandling")
        @PathVariable("behandlingId") behandlingId: UUID,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::deleteSattPaaVent.name,
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val modified = behandlingService.setSattPaaVent(
            behandlingId = behandlingId,
            setNull = true,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        return BehandlingEditedView(modified = modified)
    }

    @PostMapping("/{id}/fullfoer")
    fun fullfoerBehandling(
        @PathVariable("id") klagebehandlingId: UUID
    ): BehandlingFullfoertView {
        logKlagebehandlingMethodDetails(
            ::fullfoerBehandling.name,
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId,
            logger
        )

        val klagebehandling = behandlingService.ferdigstillBehandling(
            klagebehandlingId,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        return behandlingMapper.mapToKlagebehandlingFullfoertView(klagebehandling)
    }
}