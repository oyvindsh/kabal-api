package no.nav.klage.oppgave.domain

data class OppgaverQueryParams(
    val typer: List<String> = emptyList(),
    val ytelser: List<String> = emptyList(),
    val hjemler: List<String> = emptyList(),
    val sortering: Sortering? = Sortering.ELDST,
    val start: Int,
    val antall: Int
) {
    enum class Sortering {
        ELDST, NYEST
    }
}
