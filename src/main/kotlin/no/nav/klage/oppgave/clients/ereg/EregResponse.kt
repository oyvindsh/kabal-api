package no.nav.klage.oppgave.clients.ereg

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Organisasjon(
    val navn: Navn,
    val organisasjonsnummer: String,
    val type: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Navn(
    val navnelinje1: String?,
    val navnelinje2: String?,
    val navnelinje3: String?,
    val navnelinje4: String?,
    val navnelinje5: String?,
    val redigertnavn: String?
) {
    fun sammensattNavn(): String =
        listOfNotNull(navnelinje1, navnelinje2, navnelinje3, navnelinje4, navnelinje5).joinToString(separator = " ")
}
