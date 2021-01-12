package no.nav.klage.oppgave.pdl

data class PdlPerson(
    val foedselsnr: String,
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val beskyttelsesbehov: Beskyttelsesbehov?
)

enum class Beskyttelsesbehov {
    STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG
}
