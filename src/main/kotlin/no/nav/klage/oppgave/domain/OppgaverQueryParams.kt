package no.nav.klage.oppgave.domain

data class OppgaverQueryParams(
    val typer: List<String> = emptyList(),
    val ytelser: List<String> = emptyList(),
    val hjemler: List<String> = emptyList(),
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null,
    val tildeltSaksbehandler: String? = null
) {
    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Projeksjon {
        UTVIDET
    }
}
