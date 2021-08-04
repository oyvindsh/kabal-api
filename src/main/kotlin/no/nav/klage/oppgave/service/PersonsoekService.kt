package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.pdl.graphql.PdlClient
import no.nav.klage.oppgave.clients.pdl.graphql.SoekPersonResponse
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.personsoek.PersonSoekResponse
import no.nav.klage.oppgave.domain.personsoek.PersonSoekResponseList
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

    fun personsoek(input: KlagebehandlingerSearchCriteria): PersonSoekResponseList {
        return if (input.isFnrSoek()) {
            fnrSoek(input)
        } else {
            navnSoek(input)
        }
    }

    private fun fnrSoek(input: KlagebehandlingerSearchCriteria): PersonSoekResponseList {
        val liste = esSoek(input)
        logger.debug("Personsøk with fnr: Got ${liste.size} hits from ES")
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

    private fun navnSoek(input: KlagebehandlingerSearchCriteria): PersonSoekResponseList {
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
        return PersonSoekResponseList(mapped?.size ?: 0, mapped ?: listOf())
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
