package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.mapper.KlagebehandlingListMapper
import no.nav.klage.oppgave.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.ElasticsearchService
import no.nav.klage.oppgave.service.PersonsoekService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
//TODO: Er det litt merkelig med "ansatte" på rot i path her?
@RequestMapping("/ansatte")
class KlagebehandlingListController(
    private val klagebehandlingListMapper: KlagebehandlingListMapper,
    private val elasticsearchService: ElasticsearchService,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
    private val personsoekService: PersonsoekService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent oppgaver for en ansatt",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til."
    )
    @GetMapping("/{navIdent}/klagebehandlinger", produces = ["application/json"])
    fun getOppgaver(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): KlagebehandlingerListRespons {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)

        validateRettigheter(queryParams, navIdent)

        val valgtEnhet = enhetFromInput(queryParams.enhet)
        val searchCriteria = if (queryParams.temaer.isEmpty()) {
            klagebehandlingerSearchCriteriaMapper.toSearchCriteria(
                navIdent,
                queryParams.copy(temaer = valgtEnhet.temaer.map { it.id }),
                valgtEnhet
            )
        } else {
            klagebehandlingerSearchCriteriaMapper.toSearchCriteria(navIdent, queryParams, valgtEnhet)
        }

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet(),
                searchCriteria.ferdigstiltFom != null,
                searchCriteria.saksbehandler,
                valgtEnhet.temaer
            )
        )
    }

    /*
        Does user have the rights to get all tildelte oppgaver?
     */
    private fun validateRettigheter(
        queryParams: KlagebehandlingerQueryParams,
        navIdent: String
    ) {
        if (queryParams.erTildeltSaksbehandler == true && queryParams.tildeltSaksbehandler == null) {
            if (!canSeeTildelteOppgaver()) {
                val message = "$navIdent har ikke tilgang til å se alle tildelte oppgaver."
                logger.warn(message)
                throw MissingTilgangException(message)
            }
        }
    }

    private fun canSeeTildelteOppgaver() = innloggetSaksbehandlerRepository.erLeder() ||
            innloggetSaksbehandlerRepository.erFagansvarlig() || true

    //FIXME remove when not in use anymore
    @ApiOperation(
        value = "Hent oppgaver som gjelder en gitt person",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
    )
    @PostMapping("/{navIdent}/klagebehandlinger/personsoek", produces = ["application/json"])
    fun getOppgaverOmPerson(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @RequestBody input: PersonSoekInput
    ): KlagebehandlingerPersonSoekListRespons {
        validateNavIdent(navIdent)

        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(navIdent, input)
        val personsoekResponse = personsoekService.personsoek(searchCriteria)
        val saksbehandler = innloggetSaksbehandlerRepository.getInnloggetIdent()
        val valgtEnhet = enhetFromInput(input.enhet)

        return KlagebehandlingerPersonSoekListRespons(
            antallTreffTotalt = personsoekResponse.size,
            personer = klagebehandlingListMapper.mapPersonSoekResponseToPersonSoekListView(
                personSoekResponse = personsoekResponse,
                viseUtvidet = searchCriteria.isProjectionUtvidet(),
                saksbehandler = saksbehandler,
                tilgangTilTemaer = valgtEnhet.temaer
            )
        )
    }

    @ApiOperation(
        value = "Hent antall utildelte klagebehandlinger der fristen gått ut",
        notes = "Teller opp alle utildelte klagebehandlinger der fristen gått ut."
    )
    @GetMapping("/{navIdent}/antallklagebehandlingermedutgaattefrister", produces = ["application/json"])
    fun getAntallUtgaatteFrister(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        queryParams: KlagebehandlingerQueryParams
    ): AntallUtgaatteFristerResponse {
        logger.debug("Params: {}", queryParams)
        validateNavIdent(navIdent)
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchService.countByCriteria(
                klagebehandlingerSearchCriteriaMapper.toFristUtgaattIkkeTildeltSearchCriteria(
                    navIdent,
                    queryParams
                )
            )
        )
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

    private fun enhetFromInput(enhetId: String): EnhetMedLovligeTemaer =
        enhetId.let { saksbehandlerService.getEnheterMedTemaerForSaksbehandler().enheter.find { enhet -> enhet.enhetId == it } }
            ?: throw IllegalArgumentException("Saksbehandler har ikke tilgang til angitt enhet")
}

