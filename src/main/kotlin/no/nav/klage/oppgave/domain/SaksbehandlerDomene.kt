package no.nav.klage.oppgave.domain

import no.nav.klage.oppgave.domain.kodeverk.Tema

data class EnheterMedLovligeTemaer(val enheter: List<EnhetMedLovligeTemaer>)

data class EnhetMedLovligeTemaer(val enhetId: String, val navn: String, val temaer: List<Tema>)