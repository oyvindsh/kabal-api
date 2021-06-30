package no.nav.klage.oppgave.domain.personsoek

import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import java.time.LocalDate

data class PersonSoekResponseList(
    val antallTreffTotalt: Int,
    val liste: List<PersonSoekResponse>
)

data class PersonSoekResponse(
    val fnr: String,
    val navn: String?,
    val foedselsdato: LocalDate?,
    val klagebehandlinger: List<EsKlagebehandling>
)
