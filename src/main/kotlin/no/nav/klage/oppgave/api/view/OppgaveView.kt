package no.nav.klage.oppgave.api.view

import java.time.LocalDate

const val HJEMMEL = "HJEMMEL"

data class OppgaverRespons(
    val antallTreffTotalt: Int,
    val oppgaver: List<Oppgave>
)

data class Oppgave(
    val id: String,
    val person: Person? = null,
    val type: String,
    val tema: String,
    val hjemmel: String?,
    val frist: LocalDate?,
    val versjon: Int
) {
    data class Person(
        val fnr: String,
        val navn: String
    )
}