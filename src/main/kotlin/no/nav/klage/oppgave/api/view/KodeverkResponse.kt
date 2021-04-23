package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.*

data class KodeverkResponse(
    val eoes: List<Kode> = Eoes.values().asList().toDto(),
    val grunn: List<Kode> = Grunn.values().asList().toDto(),
    val hjemmel: List<Kode> = Hjemmel.values().asList().toDto(),
    val raadfoertMedLege: List<Kode> = RaadfoertMedLege.values().asList().toDto(),
    val type: List<Kode> = Type.values().asList().toDto(),
    val tema: List<Kode> = Tema.values().asList().toDto(),
    val utfall: List<Kode> = Utfall.values().asList().toDto(),
)

data class KodeDto(override val id: Int, override val navn: String, override val beskrivelse: String) : Kode

fun List<Kode>.toDto() = map { KodeDto(it.id, it.navn, it.beskrivelse) }
