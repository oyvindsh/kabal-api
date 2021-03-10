package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.facade.KlagebehandlingFacade
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.domain.AuditLogEvent.Action.KLAGEBEHANDLING_VIEW
import no.nav.klage.oppgave.domain.AuditLogEvent.Decision.ALLOW
import no.nav.klage.oppgave.domain.klage.KvalitetsvurderingInput
import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.RaadfoertMedLege
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.exceptions.ValidationException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}")
    fun getKlagebehandling(
        @PathVariable("id") klagebehandlingId: String
    ): KlagebehandlingView {
        logger.debug(
            "getKlagebehandling is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingFacade.getKlagebehandling(klagebehandlingId.toUUIDOrException()).also {
            auditLogger.log(
                AuditLogEvent(
                    navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent(),
                    action = KLAGEBEHANDLING_VIEW,
                    decision = ALLOW,
                    personFnr = it.foedselsnummer
                )
            )
        }
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
    fun putKvalitetsvurdering(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KvalitetsvurderingView
    ): KvalitetsvurderingView {
        logger.debug(
            "putKvalitetsvurdering is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        val kvalitetsvurdering = parseAndValidateKvalitetsvurdering(input)
        return klagebehandlingFacade.updateKvalitetsvurdering(
            klagebehandlingId.toUUIDOrException(),
            kvalitetsvurdering
        )
    }

    private fun parseAndValidateKvalitetsvurdering(input: KvalitetsvurderingView): KvalitetsvurderingInput {
        val grunn = try {
            input.grunn?.let { Grunn.of(input.grunn) }
        } catch (e: Exception) {
            logger.error("${input.grunn} is not a valid Grunn")
            throw ValidationException("${input.grunn} is not a valid Grunn")
        }
        val eoes = try {
            input.eoes?.let { Eoes.of(input.eoes) }
        } catch (e: Exception) {
            logger.error("${input.eoes} is not a valid Eoes")
            throw ValidationException("${input.eoes} is not a valid Eoes")
        }
        val raadfoertMedLege = try {
            input.raadfoertMedLege?.let { RaadfoertMedLege.of(input.raadfoertMedLege) }
        } catch (e: Exception) {
            logger.error("${input.raadfoertMedLege} is not a valid RaadfoertMedLege")
            throw ValidationException("${input.raadfoertMedLege} is not a valid RaadfoertMedLege")
        }
        return KvalitetsvurderingInput(
            grunn = grunn,
            eoes = eoes,
            raadfoertMedLege = raadfoertMedLege,
            internVurdering = input.internVurdering,
            sendTilbakemelding = input.sendTilbakemelding,
            tilbakemelding = input.tilbakemelding
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