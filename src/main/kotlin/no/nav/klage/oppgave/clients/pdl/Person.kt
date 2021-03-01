package no.nav.klage.oppgave.clients.pdl

data class Person(
    val foedselsnr: String,
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?,
    val navn: String?,
    val beskyttelsesbehov: Beskyttelsesbehov?
) {
    fun harBeskyttelsesbehovFortrolig() = beskyttelsesbehov == Beskyttelsesbehov.FORTROLIG

    fun harBeskyttelsesbehovStrengtFortrolig() =
        beskyttelsesbehov == Beskyttelsesbehov.STRENGT_FORTROLIG || beskyttelsesbehov == Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND
}

enum class Beskyttelsesbehov {
    STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG
}
