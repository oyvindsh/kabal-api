package no.nav.klage.oppgave.clients.ereg

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class Organisasjon(
    val navn: Navn,
    val organisasjonsnummer: String,
    val organisasjonDetaljer: OrganisasjonDetaljer,
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Navn(
        val sammensattnavn: String
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OrganisasjonDetaljer(
        val opphoersdato: LocalDate?
    )

    //is "ifAfter" necessary?
    fun isActive() = organisasjonDetaljer.opphoersdato == null || organisasjonDetaljer.opphoersdato.isAfter(LocalDate.now())
}