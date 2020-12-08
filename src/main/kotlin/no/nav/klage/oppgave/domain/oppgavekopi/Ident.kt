package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate

data class Ident(
    val identType: IdentType,
    val verdi: String,
    val folkeregisterident: String? = null,
    val registrertDato: LocalDate? = null
)
