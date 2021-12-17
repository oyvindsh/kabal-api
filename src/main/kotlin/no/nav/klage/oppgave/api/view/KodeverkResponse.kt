package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.*
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.Registreringshjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseTilRegistreringshjemler

data class KodeverkResponse(
    val registreringshjemler: List<KodeDto> = getRegistreringshjemler(),
    val type: List<Kode> = Type.values().asList().toDto(),
    val tema: List<Kode> = Tema.values().asList().toDto(),
    val ytelser: List<YtelseKode> = getYtelser(),
    val utfall: List<Kode> = Utfall.values().asList().toDto(),
    val partIdType: List<Kode> = PartIdType.values().asList().toDto(),
//    val rolle: List<Kode> = Rolle.values().asList().toDto(),
    val fagsystem: List<Kode> = Fagsystem.values().asList().toDto(),
    val hjemmel: List<Kode> = getHjemlerAsKoder(),

    //TODO remove when not in use by FE
    val ytelse: List<Kode> = Ytelse.values().asList().toDto(),
)

fun getYtelser(): List<YtelseKode> =
    Ytelse.values().map { ytelse ->
        YtelseKode(
            id = ytelse.id,
            navn = ytelse.navn,
            beskrivelse = ytelse.beskrivelse,
            lovKildeToRegistreringshjemler = ytelseToLovKildeToRegistreringshjemmel[ytelse] ?: emptyList()
        )
    }

val ytelseToLovKildeToRegistreringshjemmel: Map<Ytelse, List<LovKildeToRegistreringshjemler>> =
    ytelseTilRegistreringshjemler.mapValues { (_, hjemler) ->
        hjemler.groupBy(
            { hjemmel -> hjemmel.lovKilde },
            { hjemmel -> HjemmelDto(hjemmel.id, hjemmel.spesifikasjon) }
        ).map { hjemmel ->
            LovKildeToRegistreringshjemler(
                hjemmel.key.toDto(),
                hjemmel.value
            )
        }
    }

fun getRegistreringshjemler(): List<KodeDto> =
    Registreringshjemmel.values().map {
        KodeDto(
            id = it.id,
            navn = it.lovKilde.beskrivelse + " - " + it.spesifikasjon,
            beskrivelse = it.lovKilde.navn + " - " + it.spesifikasjon,
        )
    }

data class YtelseKode(
    val id: String,
    val navn: String,
    val beskrivelse: String,
    val lovKildeToRegistreringshjemler: List<LovKildeToRegistreringshjemler>,
)

data class LovKildeToRegistreringshjemler(val lovkilde: KodeDto, val registreringshjemler: List<HjemmelDto>)

data class HjemmelDto(val id: String, val navn: String)

data class KodeDto(override val id: String, override val navn: String, override val beskrivelse: String) : Kode

fun Kode.toDto() = KodeDto(id, navn, beskrivelse)

fun List<Kode>.toDto() = map { it.toDto() }

fun getHjemlerAsKoder(): List<Kode> {
    return Hjemmel.values().map {
        KodeDto(
            id = it.id,
            navn = it.lovKilde.beskrivelse + " - " + it.spesifikasjon,
            beskrivelse = it.lovKilde.navn + " - " + it.spesifikasjon,
        )
    }
}