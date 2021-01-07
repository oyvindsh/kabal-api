package no.nav.klage.oppgave.domain

import no.nav.klage.oppgave.clients.axsys.Tilganger

const val YTELSE_SYK = "Sykepenger"
const val YTELSE_FOR = "Foreldrepenger"

const val TEMA_SYK = "SYK"
const val TEMA_FOR = "FOR"

data class EnheterMedLovligeYtelser(val enheter: List<EnhetMedLovligeYtelser>)

data class EnhetMedLovligeYtelser(val enhetId: String, val navn: String, val ytelser: List<String>)

fun Tilganger.mapToInterntDomene(): EnheterMedLovligeYtelser =
    EnheterMedLovligeYtelser(this.enheter.map {
        EnhetMedLovligeYtelser(
            it.enhetId,
            it.navn,
            it.fagomrader.mapNotNull { mapTemaTilYtelse(it) })
    })

fun mapTemaTilYtelse(tema: String): String? =
    when (tema) {
        TEMA_SYK -> YTELSE_SYK
        TEMA_FOR -> YTELSE_FOR
        else -> null
    }
