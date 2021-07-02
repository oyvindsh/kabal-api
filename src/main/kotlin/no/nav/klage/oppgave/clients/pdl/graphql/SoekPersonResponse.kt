package no.nav.klage.oppgave.clients.pdl.graphql

data class SoekPersonResponse(
    val data: SoekPersonData?,
    val errors: List<PdlError>? = null
)

data class SoekPersonData(
    val sokPerson: SoekPersonResult?
)

data class SoekPersonResult(
    val totalHits: Int,
    val pageNumber: Int,
    val totalPages: Int,
    val hits: List<SoekPersonHit>
)

data class SoekPersonHit(
    val person: PdlSoekPerson
)

data class PdlSoekPerson(
    val folkeregisteridentifikator: List<Folkeregisteridentifikator>,
    val navn: List<Navn>,
    val adressebeskyttelse: List<PdlPerson.Adressebeskyttelse>,
    val foedsel: List<Foedsel>
) {
    data class Folkeregisteridentifikator(
        val identifikasjonsnummer: String
    )

    data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
    ) {
        override fun toString(): String {
            return "$fornavn $etternavn"
        }
    }

    data class Foedsel(
        val foedselsdato: String
    )
}
