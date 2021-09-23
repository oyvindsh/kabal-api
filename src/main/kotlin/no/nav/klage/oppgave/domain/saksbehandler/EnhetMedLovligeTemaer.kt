package no.nav.klage.oppgave.domain.saksbehandler

import no.nav.klage.oppgave.domain.kodeverk.Tema

data class EnhetMedLovligeTemaer(val enhetId: String, val navn: String, val temaer: List<Tema>)