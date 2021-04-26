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
    val hjemlerPerTema: List<HjemlerPerTema> = hjemlerPerTema()
)

data class KodeDto(override val id: String, override val navn: String, override val beskrivelse: String) : Kode

data class HjemlerPerTema(val temaId: String, val hjemler: List<KodeDto>)

fun List<Kode>.toDto() = map { it.toDto() }

fun Kode.toDto() = KodeDto(id, navn, beskrivelse)

fun hjemlerPerTema(): List<HjemlerPerTema> =
    listOf(
        HjemlerPerTema(
            Tema.OMS.id,
            Hjemmel.values().filter { it.kapittelOgParagraf != null && it.kapittelOgParagraf.kapittel == 9 }.toDto()
                    + Hjemmel.FTL.toDto()
        ),
        HjemlerPerTema(
            Tema.SYK.id,
            Hjemmel.values().filter { it.kapittelOgParagraf != null && it.kapittelOgParagraf.kapittel == 8 }.toDto()
                    + Hjemmel.FTL.toDto()
        ),
    )
