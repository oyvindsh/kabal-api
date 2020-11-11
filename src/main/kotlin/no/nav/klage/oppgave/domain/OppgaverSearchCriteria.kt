package no.nav.klage.oppgave.domain

data class OppgaverSearchCriteria(
    val typer: List<String> = emptyList(),
    val ytelser: List<String> = emptyList(),
    val hjemler: List<String> = emptyList(),
    val order: Order? = null,
    val offset: Int,
    val limit: Int,
    val saksbehandler: String? = null,
    var enhetsnr: String? = null,
    val projection: Projection? = null
) {
    enum class Order {
        ASC, DESC
    }

    enum class Projection {
        UTVIDET
    }
}