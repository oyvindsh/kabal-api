package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.exceptions.OppgaveIdWrongFormatException
import no.nav.klage.oppgave.service.OppgaveSearchCriteria
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class OppgaveController(val oppgaveService: OppgaveService, val saksbehandlerService: SaksbehandlerService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(value = "Hent oppgaver", notes = "Henter alle oppgaver som saksbehandler har tilgang til.")
    @GetMapping("/ansatte/{navIdent}/ikketildelteoppgaver", produces = ["application/json"])
    fun getOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Klage eller anke")
        @RequestParam(name = "type", required = false) type: String,
        @RequestParam(required = false) ytelse: String,
        @RequestParam(required = false) hjemmel: String,
        @RequestParam(required = false) orderby: String,
        @ApiParam(value = "Order {asc|desc}")
        @RequestParam(required = false) order: String,
    ): List<OppgaveView> {
        TODO()
    }


    @GetMapping("/tilganger")
    fun getTilganger(): Tilganger {
        return saksbehandlerService.getTilgangerForSaksbehandler()
    }

    @GetMapping("/oppgaver")
    fun getOppgaver(
        @RequestParam(name = "erTildelt", required = false) erTildelt: Boolean?,
        @RequestParam(name = "saksbehandler", required = false) saksbehandler: String?,
    ): List<OppgaveView> {
        logger.debug("getOppgaver is requested")
        return if (erTildelt == null && saksbehandler == null) {
            oppgaveService.getOppgaver()
        } else {
            oppgaveService.searchOppgaver(
                OppgaveSearchCriteria(
                    erTildeltSaksbehandler = erTildelt,
                    saksbehandler = saksbehandler
                )
            )
        }
    }

    @GetMapping("/oppgaver/{id}")
    fun getOppgave(@PathVariable("id") oppgaveId: String): OppgaveView {
        logger.debug("getOppgave is requested: {}", oppgaveId)

        return oppgaveService.getOppgave(oppgaveId.toLongOrException())
    }

    @PutMapping("/oppgaver/{id}/hjemmel")
    fun setHjemmel(
        @PathVariable("id") oppgaveId: String,
        @RequestBody hjemmelUpdate: HjemmelUpdate
    ): ResponseEntity<OppgaveView> {
        logger.debug("setHjemmel is requested")
        val oppgave =
            oppgaveService.setHjemmel(oppgaveId.toLongOrException(), hjemmelUpdate.hjemmel, hjemmelUpdate.versjon)
        val uri = MvcUriComponentsBuilder
            .fromMethodName(OppgaveController::class.java, "getOppgave", oppgaveId)
            .buildAndExpand(oppgaveId).toUri()
        return ResponseEntity.ok().location(uri).body(oppgave)
    }

    @PutMapping("/oppgaver/{id}/saksbehandler")
    fun setAssignedSaksbehandler(
        @PathVariable("id") oppgaveId: String,
        @RequestBody saksbehandlerUpdate: SaksbehandlerUpdate
    ): ResponseEntity<OppgaveView> {
        logger.debug("setAssignedSaksbehandler is requested")
        val oppgave = oppgaveService.assignOppgave(
            oppgaveId.toLongOrException(),
            saksbehandlerUpdate.ident,
            saksbehandlerUpdate.versjon
        )
        val uri = MvcUriComponentsBuilder
            .fromMethodName(OppgaveController::class.java, "getOppgave", oppgaveId)
            .buildAndExpand(oppgaveId).toUri()
        return ResponseEntity.ok().location(uri).body(oppgave)
    }

    @GetMapping("/oppgaver/randomhjemler")
    fun endreHjemlerAtRandomViolatingGetRpcStyleAndTotallyNotRestish(): List<OppgaveView> {
        logger.debug("endreHjemlerAtRandomViolatingGetRpcStyleAndTotallyNotRestish is requested")
        return oppgaveService.assignRandomHjemler()
    }

    private fun String?.toLongOrException() =
        this?.toLongOrNull() ?: throw OppgaveIdWrongFormatException("OppgaveId could not be parsed as a Long")

}

data class HjemmelUpdate(val hjemmel: String, val versjon: Int? = null)

data class SaksbehandlerUpdate(val ident: String, val versjon: Int? = null)