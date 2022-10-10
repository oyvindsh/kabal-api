package no.nav.klage.oppgave.api.view

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Saksbehandlertildeling(
    val navIdent: String,
)