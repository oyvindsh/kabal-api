package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.*

data class KodeverkResponse(
    val eoes: List<Kode> = Eoes.values().asList(),
    val grunn: List<Kode> = Grunn.values().asList(),
    val hjemmel: List<Kode> = Hjemmel.values().asList(),
    val raadfoertMedLege: List<Kode> = RaadfoertMedLege.values().asList(),
    val sakstype: List<Kode> = Type.values().asList(),
    val tema: List<Kode> = Tema.values().asList(),
    val utfall: List<Kode> = Utfall.values().asList(),
)