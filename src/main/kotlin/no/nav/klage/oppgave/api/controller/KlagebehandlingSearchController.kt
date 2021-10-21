package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.oppgave.api.mapper.KlagebehandlingListMapper
import no.nav.klage.oppgave.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.oppgave.api.view.FnrSearchResponse
import no.nav.klage.oppgave.api.view.NameSearchResponse
import no.nav.klage.oppgave.api.view.SearchPersonByFnrInput
import no.nav.klage.oppgave.api.view.SearchPersonByNameInput
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.PersonsoekService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/search")
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingSearchController(
    private val klagebehandlingMapper: KlagebehandlingListMapper,
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
        value = "Søk oppgaver som gjelder en gitt person",
        notes = "Finner alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
    )
    @PostMapping("/fnr", produces = ["application/json"])
    fun getFnrSearchResponse(@RequestBody input: SearchPersonByFnrInput): FnrSearchResponse? {
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(input)
        val personSoekHits = personsoekService.fnrSearch(searchCriteria)
        val saksbehandler = innloggetSaksbehandlerRepository.getInnloggetIdent()
        val valgtEnhet = saksbehandlerService.findValgtEnhet(saksbehandler)
        return klagebehandlingMapper.mapPersonSoekHitsToFnrSearchResponse(
            personSoekHits = personSoekHits,
            saksbehandler = saksbehandler,
            tilgangTilTemaer = valgtEnhet.temaer
        )
    }

    @ApiOperation(
        value = "Hent oppgaver som gjelder en gitt person",
        notes = "Henter alle oppgaver som saksbehandler har tilgang til som omhandler en gitt person."
    )
    @PostMapping("/name", produces = ["application/json"])
    fun getNameSearchResponse(@RequestBody input: SearchPersonByNameInput): NameSearchResponse {
        val people = personsoekService.nameSearch(input.query)
        return NameSearchResponse(
            people = people.map {
                NameSearchResponse.PersonView(
                    fnr = it.fnr,
                    name = it.name
                )
            }
        )
    }
}

