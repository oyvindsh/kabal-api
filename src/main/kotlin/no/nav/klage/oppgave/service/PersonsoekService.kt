package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.mapper.KlagebehandlingerSearchCriteriaMapper
import no.nav.klage.oppgave.api.view.PersonSoekInput
import no.nav.klage.oppgave.clients.pdl.graphql.PdlClient
import no.nav.klage.oppgave.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.personsoek.PersonSoekResponse
import no.nav.klage.oppgave.domain.personsoek.PersonSoekResponseList
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.time.LocalDate

@Service
class PersonsoekService(
    private val pdlClient: PdlClient,
    private val elasticsearchService: ElasticsearchService,
    private val klagebehandlingerSearchCriteriaMapper: KlagebehandlingerSearchCriteriaMapper
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun personsoek(navIdent: String, input: PersonSoekInput): PersonSoekResponseList {
        return if(input.isFnrSoek()) {
            fnrSoek(navIdent, input)
        } else {
            navnSoek(navIdent, input)
        }
    }

    private fun fnrSoek(navIdent: String, input: PersonSoekInput): PersonSoekResponseList {
        val liste = esSoek(navIdent, input)
        val mapped = liste.groupBy { it.sakenGjelderFnr }.map { (key, value) ->
            PersonSoekResponse(
                fnr = key!!,
                navn = value.first().sakenGjelderNavn,
                foedselsdato = null,
                klagebehandlinger = value
            )
        }
        return PersonSoekResponseList(liste.size, mapped)
    }

    private fun navnSoek(navIdent: String, input: PersonSoekInput): PersonSoekResponseList {
        val pdlResponse = pdlClient.personsok(input.soekString)
        verifyPdlResponse(pdlResponse)
        val mapped = pdlResponse.data?.soekPerson?.hits?.map {
            val fnr = it.person.folkeregisteridentifikator.identifikasjonsnummer
            val klagebehandlinger = esSoek(navIdent, input.copy(soekString = fnr))
            PersonSoekResponse(
                fnr = fnr,
                navn = it.person.navn.toString(),
                foedselsdato = LocalDate.parse(it.person.foedsel.foedselsdato),
                klagebehandlinger = klagebehandlinger
            )
        }
        return PersonSoekResponseList(mapped?.size ?: 0, mapped ?: listOf())
    }

    private fun esSoek(navIdent: String, input: PersonSoekInput): List<EsKlagebehandling> {
        val searchCriteria = klagebehandlingerSearchCriteriaMapper.toSearchCriteria(navIdent, input)
        val esResponse = elasticsearchService.findByCriteria(searchCriteria)
        return esResponse.searchHits.map { it.content }
    }

    private fun verifyPdlResponse(response: SoekPersonResponse) {
        if (response.errors != null) {
            logger.error("Error from PDL, see secure logs")
            secureLogger.error("Error from pdl ${response.errors}")
            throw RuntimeException("Søkefeil i PDL")
        }
    }
}
