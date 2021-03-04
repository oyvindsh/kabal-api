package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.*

data class KodeverkResponse(
    val eoes: List<KodeMedIntId> = Eoes.values().asList().map { KodeMedIntId(it.id, it.navn, it.beskrivelse) },
    val grunn: List<KodeMedIntId> = Grunn.values().asList().map { KodeMedIntId(it.id, it.navn, it.beskrivelse) },
    val kilde: List<Kilde> = Kilde.values().asList(),
    val lov: List<KodeMedIntId> = Lov.values().asList().map { KodeMedIntId(it.id, it.navn, it.beskrivelse) },
    val raadfoertMedLege: List<KodeMedIntId> = RaadfoertMedLege.values().asList()
        .map { KodeMedIntId(it.id, it.navn, it.beskrivelse) },
    val sakstype: List<KodeMedStringId> = Sakstype.values().asList()
        .map { KodeMedStringId(it.id, it.navn, it.beskrivelse) },
    val tema: List<KodeMedStringId> = Tema.values().asList().map { KodeMedStringId(it.id, it.navn, it.beskrivelse) },
    val utfall: List<KodeMedIntId> = Utfall.values().asList().map { KodeMedIntId(it.id, it.navn, it.beskrivelse) }
)

data class KodeMedIntId(val id: Int, val navn: String, val beskrivelse: String?)

data class KodeMedStringId(val id: String, val navn: String, val beskrivelse: String?)