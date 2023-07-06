package no.nav.klage.oppgave.clients.kabalinnstillinger.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

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

data class ROLSearchInput(
    val fnr: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SaksbehandlerAccess(
    val saksbehandlerIdent: String,
    val saksbehandlerName: String,
    val ytelseIdList: List<String>,
    val created: LocalDateTime?,
    val accessRightsModified: LocalDateTime?,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TildelteYtelserResponse(
    val ytelseIdList: List<String>
)