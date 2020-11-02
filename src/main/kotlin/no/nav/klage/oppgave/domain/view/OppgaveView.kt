package no.nav.klage.oppgave.domain.view

import java.time.LocalDate

const val HJEMMEL = "HJEMMEL"
const val TYPE_KLAGE = "klage"
const val TYPE_FEILUTBETALING = "feilutbetaling"

data class OppgaveView(
    val id: String,
    val bruker: Bruker,
    val type: String,
    val ytelse: String,
    val hjemmel: String,
    val frist: LocalDate?,
    val saksbehandler: Saksbehandler? = null,
    val versjon: Int?
) {
    data class Bruker(
        val fnr: String,
        val navn: String
    )

    data class Saksbehandler(
        val ident: String,
        val navn: String
    )
}