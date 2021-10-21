package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.pdl.graphql.PdlClient
import no.nav.klage.oppgave.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.personsoek.Person
import no.nav.klage.oppgave.domain.personsoek.PersonSoekResponse
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class PersonsoekService(
    private val pdlClient: PdlClient,
    private val elasticsearchService: ElasticsearchService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun personsoek(input: KlagebehandlingerSearchCriteria): List<PersonSoekResponse> {
        return if (input.isFnrSoek()) {
            fnrSearch(input)
        } else {
            navnSoek(input)
        }
    }

    fun fnrSearch(input: KlagebehandlingerSearchCriteria): List<PersonSoekResponse> {
        val searchHits = esSoek(input)
        logger.debug("Personsøk with fnr: Got ${searchHits.size} hits from ES")
        val listOfPersonSoekResponse = searchHits.groupBy { it.sakenGjelderFnr }.map { (key, value) ->
            PersonSoekResponse(
                fnr = key!!,
                navn = value.first().sakenGjelderNavn,
                foedselsdato = null,
                klagebehandlinger = value
            )
        }
        return listOfPersonSoekResponse
    }

    //FIXME remove when not in use anymore
    private fun navnSoek(input: KlagebehandlingerSearchCriteria): List<PersonSoekResponse> {
        val pdlResponse = pdlClient.personsok(input.raw)
        secureLogger.debug("Fetched data from PDL søk: $pdlResponse")
        verifyPdlResponse(pdlResponse)
        val fnrList = pdlResponse.collectFnr()

        var klagebehandlinger: Map<String?, List<EsKlagebehandling>> = emptyMap()
        //Only fetch klagebehandlinger when there is only one hit
        if (fnrList.size == 1) {
            klagebehandlinger = esSoek(input.copy(foedselsnr = fnrList)).groupBy { it.klagerFnr }
        }
        val mapped = pdlResponse.data?.sokPerson?.hits?.map { personHit ->
            val fnr = personHit.person.folkeregisteridentifikator.first().identifikasjonsnummer
            PersonSoekResponse(
                fnr = fnr,
                navn = personHit.person.navn.first().toString(),
                foedselsdato = if (personHit.person.foedsel.isNotEmpty()) {
                    LocalDate.parse(personHit.person.foedsel.first().foedselsdato)
                } else null,
                klagebehandlinger = klagebehandlinger[fnr] ?: listOf()
            )
        }
        return mapped ?: emptyList()
    }

    fun nameSearch(name: String): List<Person> {
        val pdlResponse = pdlClient.personsok(name)
        secureLogger.debug("Fetched data from PDL søk: $pdlResponse")
        verifyPdlResponse(pdlResponse)

        val people = pdlResponse.data?.sokPerson?.hits?.map { personHit ->
            Person(
                fnr = personHit.person.folkeregisteridentifikator.first().identifikasjonsnummer,
                name = personHit.person.navn.first().toString()
            )
        }
        return people ?: emptyList()
    }

    private fun esSoek(input: KlagebehandlingerSearchCriteria): List<EsKlagebehandling> {
        val esResponse = elasticsearchService.findByCriteria(input)
        return esResponse.searchHits.map { it.content }
    }

    private fun verifyPdlResponse(response: SoekPersonResponse) {
        if (response.errors != null) {
            logger.error("Error from PDL, see secure logs")
            secureLogger.error("Error from pdl ${response.errors}")
            throw RuntimeException("Søkefeil i PDL")
        }
    }

    private fun SoekPersonResponse.collectFnr(): List<String> =
        this.data?.sokPerson?.hits?.map {
            it.person.folkeregisteridentifikator.first().identifikasjonsnummer
        } ?: listOf()
}
