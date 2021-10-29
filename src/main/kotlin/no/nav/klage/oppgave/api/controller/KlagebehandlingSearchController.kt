package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.oppgave.api.mapper.KlagebehandlingListMapper
import no.nav.klage.oppgave.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Sivilstand
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.kodeverk.TemaTilgjengeligeForEktefelle
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.ElasticsearchService
import no.nav.klage.oppgave.service.PersonsoekService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/search")
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingSearchController(
    private val klagebehandlingListMapper: KlagebehandlingListMapper,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerService: SaksbehandlerService,
    private val personsoekService: PersonsoekService,
    private val environment: Environment,
    private val pdlFacade: PdlFacade,
    private val elasticsearchService: ElasticsearchService
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
        val valgtEnhet = enhetFromInputOrInnstillinger(input.enhet)
        return klagebehandlingListMapper.mapPersonSoekHitsToFnrSearchResponse(
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

    @PostMapping("/relaterte")
    fun getRelaterteKlagebehandlinger(
        @RequestBody input: SearchPersonByFnrInput
    ): KlagebehandlingerListRespons {
        //TODO: Move logic to PersonsoekService
        val lovligeTemaer = enhetFromInputOrInnstillinger(input.enhet).temaer
        val sivilstand: Sivilstand? = pdlFacade.getPersonInfo(input.query).sivilstand

        val searchCriteria = KlagebehandlingerSearchCriteria(
            statuskategori = KlagebehandlingerSearchCriteria.Statuskategori.ALLE,
            ferdigstiltFom = LocalDate.now().minusMonths(12),
            foedselsnr = listOf(input.query),
            extraPersonAndTema = sivilstand?.let {
                KlagebehandlingerSearchCriteria.ExtraPersonAndTema(
                    foedselsnr = it.foedselsnr,
                    temaer = TemaTilgjengeligeForEktefelle.temaerTilgjengeligForEktefelle(environment).toList()
                )
            },
            offset = 0,
            limit = 100,
            projection = KlagebehandlingerSearchCriteria.Projection.UTVIDET,
        )

        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingListMapper.mapEsKlagebehandlingerToListView(
                esKlagebehandlinger = esResponse.searchHits.map { it.content },
                viseUtvidet = true,
                viseFullfoerte = true,
                saksbehandler = searchCriteria.saksbehandler,
                tilgangTilTemaer = lovligeTemaer,
                sivilstand = sivilstand
            )
        )
    }

    private fun enhetFromInputOrInnstillinger(enhetId: String?): EnhetMedLovligeTemaer =
        enhetId?.let { saksbehandlerService.getEnheterMedTemaerForSaksbehandler().enheter.find { enhet -> enhet.enhetId == it } }
            ?: saksbehandlerService.findValgtEnhet(innloggetSaksbehandlerRepository.getInnloggetIdent())
}

