package no.nav.klage.oppgave.api.view

data class MedunderskrivereInput(
    val ytelse: String,
    val fnr: String,
    val enhet: String,
    val navIdent: String,
)
