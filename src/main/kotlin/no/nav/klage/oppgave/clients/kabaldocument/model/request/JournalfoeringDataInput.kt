package no.nav.klage.oppgave.clients.kabaldocument.model.request

data class JournalfoeringDataInput(
    val sakenGjelder: PartIdInput,
    val temaId: String,
    val sakFagsakId: String,
    val sakFagsystemId: String,
    val kildeReferanse: String,
    val enhet: String,
    val behandlingstema: String,
    val tittel: String,
    val brevKode: String,
    val tilleggsopplysning: TilleggsopplysningInput?
)
