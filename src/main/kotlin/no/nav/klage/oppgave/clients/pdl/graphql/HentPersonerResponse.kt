package no.nav.klage.oppgave.clients.pdl.graphql

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls


data class HentPersonerResponse(val data: HentPersonBolk?, val errors: List<PdlError>? = null)

data class HentPersonResponse(val data: PdlPerson?, val errors: List<PdlError>? = null)

data class HentPersonBolk(val hentPersonBolk: List<HentPersonBolkResult>?)

data class HentPersonBolkResult(
    val person: PdlPerson?,
    val ident: String
)

@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class PdlPerson(
    @JsonSetter(nulls = Nulls.AS_EMPTY) val adressebeskyttelse: List<Adressebeskyttelse>,
    val navn: List<Navn>,
    val kjoenn: List<Kjoenn>
) {
    data class Adressebeskyttelse(val gradering: GraderingType) {
        enum class GraderingType { STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG, UGRADERT }
    }

    data class Navn(
        val fornavn: String,
        val mellomnavn: String?,
        val etternavn: String
    )

    data class Kjoenn(val kjoenn: KjoennType?) {
        enum class KjoennType { MANN, KVINNE, UKJENT }
    }
}

data class PdlError(
    val message: String,
    val locations: List<PdlErrorLocation>,
    val path: List<String>?,
    val extensions: PdlErrorExtension
)

data class PdlErrorLocation(
    val line: Int?,
    val column: Int?
)

data class PdlErrorExtension(
    val code: String?,
    val classification: String
)
