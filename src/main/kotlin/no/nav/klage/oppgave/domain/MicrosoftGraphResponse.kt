package no.nav.klage.oppgave.domain

data class MicrosoftGraphIdentResponse(val onPremisesSamAccountName: String)

data class MicrosoftGraphNameResponse(val value: List<Value>) {
    data class Value(val onPremisesSamAccountName: String, val displayName: String)
}