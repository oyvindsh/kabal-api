package no.nav.klage.oppgave.domain.view

import java.time.LocalDate

const val HJEMMEL = "HJEMMEL"
const val TYPE_KLAGE = "klage"
const val TYPE_FEILUTBETALING = "feilutbetaling"

data class TildelteOppgaverRespons(
    val antallTreffTotalt: Int,
    val oppgaver: List<TildeltOppgave>
)

data class IkkeTildelteOppgaverRespons(
    val antallTreffTotalt: Int,
    val oppgaver: List<IkkeTildeltOppgave>
)

data class TildeltOppgave(
    val id: String,
    val bruker: Bruker,
    val type: String,
    val ytelse: String,
    val hjemmel: String?,
    val frist: LocalDate?,
    val versjon: Int
) {
    data class Bruker(
        val fnr: String,
        val navn: String
    )
}

data class IkkeTildeltOppgave(
    val id: String,
    val type: String,
    val ytelse: String,
    val hjemmel: String?,
    val frist: LocalDate,
    val versjon: Int
)