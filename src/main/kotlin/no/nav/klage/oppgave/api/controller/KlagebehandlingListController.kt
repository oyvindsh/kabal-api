package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.mapper.KlagebehandlingerQueryParamsMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.exceptions.OppgaveVersjonWrongFormatException
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingListController(
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val elasticsearchRepository: ElasticsearchRepository,
    private val klagebehandlingerQueryParamsMapper: KlagebehandlingerQueryParamsMapper,
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
    @GetMapping("/ansatte/{navIdent}/klagebehandlinger", produces = ["application/json"])
    fun getOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)
        val searchCriteria = klagebehandlingerQueryParamsMapper.toSearchCriteria(navIdent, queryParams)
        val esResponse = elasticsearchRepository.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet()
            )
        )
    }

    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en klagebehandling")
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): ResponseEntity<Void> {
        logger.debug("assignSaksbehandler is requested for klagebehandling: {}", klagebehandlingId)
        klagebehandlingService.assignKlagebehandling(
            klagebehandlingId.toUUIDOrException(),
            saksbehandlertildeling.klagebehandlingVersjon,
            saksbehandlertildeling.navIdent,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )

        val uri = MvcUriComponentsBuilder
            .fromMethodName(KlagebehandlingController::class.java, "getKlagebehandling", klagebehandlingId)
            .buildAndExpand(klagebehandlingId).toUri()
        return ResponseEntity.noContent().location(uri).build()
    }

    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Id til en klagebehandling")
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody(required = false) saksbehandlerfradeling: Saksbehandlerfradeling?
    ): ResponseEntity<Void> {
        logger.debug("unassignSaksbehandler is requested for klagebehandling: {}", klagebehandlingId)
        klagebehandlingService.assignKlagebehandling(
            klagebehandlingId.toUUIDOrException(),
            saksbehandlerfradeling?.klagebehandlingVersjon,
            null,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )

        val uri = MvcUriComponentsBuilder
            .fromMethodName(KlagebehandlingController::class.java, "getKlagebehandling", klagebehandlingId)
            .buildAndExpand(klagebehandlingId).toUri()
        return ResponseEntity.noContent().location(uri).build()
    }

    @ApiOperation(
        value = "Hent antall utildelte klagebehandlinger der fristen gått ut",
        notes = "Teller opp alle utildelte klagebehandlinger der fristen gått ut."
    )
    @GetMapping("/ansatte/{navIdent}/antallklagebehandlingermedutgaattefrister", produces = ["application/json"])
    fun getAntallUtgaatteFrister(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchRepository.countByCriteria(
                klagebehandlingerQueryParamsMapper.toFristUtgaattIkkeTildeltSearchCriteria(
                    navIdent,
                    queryParams
                )
            )
        )
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