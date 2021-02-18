package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.OppgaverQueryParamsMapper
import no.nav.klage.oppgave.api.view.OppgaverQueryParams
import no.nav.klage.oppgave.api.view.OppgaverRespons
import no.nav.klage.oppgave.api.view.Saksbehandlerfradeling
import no.nav.klage.oppgave.api.view.Saksbehandlertildeling
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.exceptions.OppgaveVersjonWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.util.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val oppgaverQueryParamsMapper: OppgaverQueryParamsMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent oppgaver for en ansatt",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/oppgaver", produces = ["application/json"])
    fun getOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: OppgaverQueryParams
    ): OppgaverRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)
        return klagebehandlingFacade.searchOppgaver(
            oppgaverQueryParamsMapper.toSearchCriteria(navIdent, queryParams)
        )
    }

    @PostMapping("/ansatte/{navIdent}/oppgaver/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en klagebehandling")
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): ResponseEntity<Void> {
        logger.debug("assignSaksbehandler is requested for klagebehandling: {}", klagebehandlingId)
        klagebehandlingFacade.assignOppgave(
            klagebehandlingId.toUUIDOrException(),
            saksbehandlertildeling.navIdent,
            saksbehandlertildeling.oppgaveversjon.versjonToLongOrException()
        )

        val uri = MvcUriComponentsBuilder
            .fromMethodName(KlagebehandlingController::class.java, "getKlagebehandling", klagebehandlingId)
            .buildAndExpand(klagebehandlingId).toUri()
        return ResponseEntity.noContent().location(uri).build()
    }

    @PostMapping("/ansatte/{navIdent}/oppgaver/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en klagebehandling")
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody saksbehandlerfradeling: Saksbehandlerfradeling
    ): ResponseEntity<Void> {
        logger.debug("unassignSaksbehandler is requested for klagebehandling: {}", klagebehandlingId)
        klagebehandlingFacade.assignOppgave(
            klagebehandlingId.toUUIDOrException(),
            null,
            saksbehandlerfradeling.oppgaveversjon.versjonToLongOrException()
        )

        val uri = MvcUriComponentsBuilder
            .fromMethodName(KlagebehandlingController::class.java, "getKlagebehandling", klagebehandlingId)
            .buildAndExpand(klagebehandlingId).toUri()
        return ResponseEntity.noContent().location(uri).build()
    }

    private fun String?.versjonToLongOrException() =
        this?.toLongOrNull()
            ?: throw OppgaveVersjonWrongFormatException("KlagebehandlingVersjon could not be parsed as an Long")

    private fun String.toUUIDOrException(): UUID =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            logger.error("KlagebehandlingId could not be parsed as an UUID", e)
            throw BehandlingsidWrongFormatException("KlagebehandlingId could not be parsed as an UUID")
        }

    private fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

    //    @PutMapping("/oppgaver/{id}/hjemmel")
//    fun setHjemmel(
//        @PathVariable("id") oppgaveId: String,
//        @RequestBody hjemmelUpdate: HjemmelUpdate
//    ): ResponseEntity<OppgaveView> {
//        logger.debug("setHjemmel is requested")
//        val oppgave =
//            oppgaveService.setHjemmel(oppgaveId.toLongOrException(), hjemmelUpdate.hjemmel, hjemmelUpdate.versjon)
//        val uri = MvcUriComponentsBuilder
//            .fromMethodName(OppgaveController::class.java, "getOppgave", oppgaveId)
//            .buildAndExpand(oppgaveId).toUri()
//        return ResponseEntity.ok().location(uri).body(oppgave)
//    }
//

}