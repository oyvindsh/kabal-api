package no.nav.klage.oppgave.domain

data class OppgaverQueryParams(
    val typer: List<String> = emptyList(),
    val ytelser: List<String> = emptyList(),
    val hjemler: List<String> = emptyList(),
    val orderBy: String?,
    val order: Order?,
    val offset: Int,
    val limit: Int
) {
    enum class Order {
        ASC, DESC
    }
}
