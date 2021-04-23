package no.nav.klage.oppgave.api.view

import java.time.LocalDate

data class KlagebehandlingerListRespons(
    val antallTreffTotalt: Int,
    val klagebehandlinger: List<KlagebehandlingListView>
)

data class KlagebehandlingListView(
    val id: String,
    val person: Person? = null,
    val type: Int,
    val tema: Int,
    val hjemmel: Int?,
    val frist: LocalDate?,
    val mottatt: LocalDate?,
    val versjon: Int,
    val klagebehandlingVersjon: Long,
    val erMedunderskriver: Boolean
) {
    data class Person(
        val fnr: String?,
        val navn: String?
    )
}