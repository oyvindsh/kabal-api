package no.nav.klage.oppgave.api.view

import java.time.LocalDate

data class KlagebehandlingerListRespons(
    val antallTreffTotalt: Int,
    val klagebehandlinger: List<KlagebehandlingListView>
)

data class KlagebehandlingListView(
    val id: String,
    val person: Person? = null,
    val type: String,
    val tema: String,
    val hjemmel: String?,
    val frist: LocalDate?,
    val mottatt: LocalDate?,
    val versjon: Int,
    val klagebehandlingVersjon: Long,
    val erMedunderskriver: Boolean = false,
    val harMedunderskriver: Boolean = false,
    val medunderskriverident: String?,
    val utfall: String?,
    val avsluttetAvSaksbehandler: LocalDate?,
    val erTildelt: Boolean,
    val tildeltSaksbehandlerident: String?,
    val tildeltSaksbehandlerNavn: String?,
    val saksbehandlerHarTilgang: Boolean,
) {
    data class Person(
        val fnr: String?,
        val navn: String?,
        val sivilstand: String? = null
    )
}

data class KlagebehandlingerPersonSoekListRespons(
    val antallTreffTotalt: Int,
    val personer: List<PersonSoekPersonView>
)

data class PersonSoekPersonView(
    val fnr: String,
    val navn: String?,
    val klagebehandlinger: List<KlagebehandlingListView>,
    val aapneKlagebehandlinger: List<KlagebehandlingListView>,
    val avsluttedeKlagebehandlinger: List<KlagebehandlingListView>
)