package no.nav.klage.oppgave.domain.view

import java.time.LocalDate

data class OppgaveView(
    val id: Int,
    val bruker: Bruker,
    val type: String,
    val ytelse: String,
    val hjemmel: List<String>,
    val frist: LocalDate?,
    val saksbehandler: String
) {
    data class Bruker(
        val fnr: String,
        val navn: String
    )
}