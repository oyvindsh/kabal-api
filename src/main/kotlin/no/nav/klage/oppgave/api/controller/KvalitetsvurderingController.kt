package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.facade.KlagebehandlingFacade
import no.nav.klage.oppgave.api.view.KvalitetsvurderingGrunnInput
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class KvalitetsvurderingController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}/kvalitetsvurdering")
    fun getKvalitetsvurdering(
        @PathVariable("id") klagebehandlingId: String
    ): KvalitetsvurderingView {
        logger.debug(
            "getKvalitetsvurdering is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingFacade.getKvalitetsvurdering(klagebehandlingId.toUUIDOrException())
    }

    @PutMapping("/klagebehandlinger/{id}/kvalitetsvurdering")
    fun putKvalitetsvurderingGrunn(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingGrunnInput
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurderingGrunn is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingFacade.updateKvalitetsvurderingGrunn(
            klagebehandlingId.toUUIDOrException(),
            input.grunn,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
    }

    private fun String.toUUIDOrException() =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            logger.error("KlagebehandlingId could not be parsed as an UUID", e)
            throw BehandlingsidWrongFormatException("KlagebehandlingId could not be parsed as an UUID")
        }
}