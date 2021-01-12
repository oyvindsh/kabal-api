package no.nav.klage.oppgave.domain

import no.nav.klage.oppgave.clients.axsys.Tilganger

const val TEMA_NAME_SYK = "Sykepenger"
const val TEMA_NAME_FOR = "Foreldrepenger"

const val TEMA_SYK = "SYK"
const val TEMA_FOR = "FOR"

const val TYPE_KLAGE = "Klage"
const val TYPE_FEILUTBETALING = "Feilutbetaling"

const val BEHANDLINGSTYPE_KLAGE = "ae0058"
const val BEHANDLINGSTYPE_FEILUTBETALING = "ae0161"

data class EnheterMedLovligeTemaer(val enheter: List<EnhetMedLovligeTemaer>)

data class EnhetMedLovligeTemaer(val enhetId: String, val navn: String, val temaer: List<String>)

fun Tilganger.mapToInterntDomene(): EnheterMedLovligeTemaer =
    EnheterMedLovligeTemaer(this.enheter.map { enhet ->
        EnhetMedLovligeTemaer(
            enhet.enhetId,
            enhet.navn,
            enhet.fagomrader.mapNotNull { mapTemaToTemaName(it) })
    })

fun mapTemaToTemaName(tema: String): String? =
    when (tema) {
        TEMA_SYK -> TEMA_NAME_SYK
        TEMA_FOR -> TEMA_NAME_FOR
        else -> null
    }
