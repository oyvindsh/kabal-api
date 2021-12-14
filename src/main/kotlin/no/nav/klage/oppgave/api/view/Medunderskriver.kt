package no.nav.klage.oppgave.api.view

data class Medunderskrivere(val tema: String?, val ytelse: String?, val medunderskrivere: List<Medunderskriver>)

//TODO: Bytt ut denne med SaksbehandlerView når FE har tatt i bruk navIdent
data class Medunderskriver(val ident: String, val navIdent: String, val navn: String)
