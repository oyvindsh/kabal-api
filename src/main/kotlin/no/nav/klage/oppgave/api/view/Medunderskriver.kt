package no.nav.klage.oppgave.api.view

data class Medunderskrivere(val tema: String?, val ytelse: String?, val medunderskrivere: List<Medunderskriver>)

//Denne er egentlig kliss lik SaksbehandlerView, forskjellen er bare ident vs navIdent..
data class Medunderskriver(val ident: String, val navn: String)
