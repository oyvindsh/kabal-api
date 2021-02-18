package no.nav.klage.oppgave.domain

import no.nav.klage.oppgave.domain.kodeverk.Tema

const val TEMA_NAME_SYK = "Sykepenger"
const val TEMA_NAME_FOR = "Foreldrepenger"

const val TEMA_SYK = "SYK"
const val TEMA_FOR = "FOR"

const val TYPE_KLAGE = "Klage"
const val TYPE_FEILUTBETALING = "Feilutbetaling"

const val BEHANDLINGSTYPE_KLAGE = "ae0058"
const val BEHANDLINGSTYPE_FEILUTBETALING = "ae0161"
const val BEHANDLINGSTYPE_ANKE = "ab0165"

data class EnheterMedLovligeTemaer(val enheter: List<EnhetMedLovligeTemaer>)

data class EnhetMedLovligeTemaer(val enhetId: String, val navn: String, val temaer: List<Tema>)