package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.OppgaverQueryParams
import no.nav.klage.oppgave.domain.view.IkkeTildelteOppgaverRespons
import no.nav.klage.oppgave.domain.view.TildelteOppgaverRespons
import no.nav.klage.oppgave.exceptions.OppgaveIdWrongFormatException
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(val oppgaveService: OppgaveService, val saksbehandlerService: SaksbehandlerService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent ikke tildelte oppgaver for en ansatt",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/ikketildelteoppgaver", produces = ["application/json"])
    fun getIkkeTildelteOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: OppgaverQueryParams
    ): IkkeTildelteOppgaverRespons {
        logger.debug("Params: {}", queryParams)
        return oppgaveService.searchIkkeTildelteOppgaver(queryParams)
    }

    @ApiOperation(
        value = "Hent tildelte oppgaver for en ansatt",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/ansatte/{navIdent}/tildelteoppgaver", produces = ["application/json"])
    fun getTildelteOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: OppgaverQueryParams
    ): TildelteOppgaverRespons {
        logger.debug("Params: {}", queryParams)
        return oppgaveService.searchTildelteOppgaver(navIdent, queryParams)
    }

//    @GetMapping("/oppgaver/{id}")
//    fun getOppgave(@PathVariable("id") oppgaveId: String): OppgaveView {
//        logger.debug("getOppgave is requested: {}", oppgaveId)
//
//        return oppgaveService.getOppgave(oppgaveId.toLongOrException())
//    }

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
//    @PutMapping("/oppgaver/{id}/saksbehandler")
//    fun setAssignedSaksbehandler(
//        @PathVariable("id") oppgaveId: String,
//        @RequestBody saksbehandlerUpdate: SaksbehandlerUpdate
//    ): ResponseEntity<OppgaveView> {
//        logger.debug("setAssignedSaksbehandler is requested")
//        val oppgave = oppgaveService.assignOppgave(
//            oppgaveId.toLongOrException(),
//            saksbehandlerUpdate.ident,
//            saksbehandlerUpdate.versjon
//        )
//        val uri = MvcUriComponentsBuilder
//            .fromMethodName(OppgaveController::class.java, "getOppgave", oppgaveId)
//            .buildAndExpand(oppgaveId).toUri()
//        return ResponseEntity.ok().location(uri).body(oppgave)
//    }

    private fun String?.toLongOrException() =
        this?.toLongOrNull() ?: throw OppgaveIdWrongFormatException("OppgaveId could not be parsed as a Long")

}

data class HjemmelUpdate(val hjemmel: String, val versjon: Int? = null)

data class SaksbehandlerUpdate(val ident: String, val versjon: Int? = null)