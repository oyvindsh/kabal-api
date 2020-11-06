package no.nav.klage.oppgave.domain

data class OppgaverSearchCriteria(
    val typer: List<String> = emptyList(),
    val ytelser: List<String> = emptyList(),
    val hjemler: List<String> = emptyList(),
    val orderBy: String? = null,
    val order: Order? = null,
    val offset: Int,
    val limit: Int,
    val saksbehandler: String? = null,
    var enhetsnr: String? = null
) {
    enum class Order {
        ASC, DESC
    }
}