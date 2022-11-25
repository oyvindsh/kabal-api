package no.nav.klage.oppgave.clients.kabalinnstillinger.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Medunderskrivere(val medunderskrivere: List<Saksbehandler>)

data class MedunderskrivereInput(
    val ytelseId: String,
    val fnr: String?,
    val enhet: String,
    val navIdent: String,
)

data class Saksbehandlere(val saksbehandlere: List<Saksbehandler>)

data class Saksbehandler(val navIdent: String, val navn: String)

data class SaksbehandlerSearchInput(
    val ytelseId: String,
    val fnr: String,
)
