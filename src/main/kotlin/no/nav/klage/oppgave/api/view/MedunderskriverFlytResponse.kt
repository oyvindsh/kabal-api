package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDateTime

data class MedunderskriverFlytResponse (
    val navn: String?,
    val navIdent: String?,
    val modified: LocalDateTime,
    val medunderskriverFlyt: MedunderskriverFlyt
)

data class MedunderskriverWrapped (
    val medunderskriver: SaksbehandlerView?,
    val modified: LocalDateTime,
    val medunderskriverFlyt: MedunderskriverFlyt
)
