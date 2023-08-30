package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime

data class RolEditedView(
    val navIdent: String?,
    val navn: String?,
    val rolStateId: String?,
    val modified: LocalDateTime,
)

data class Rols(val rols: List<Rol>) {
    data class Rol(val navIdent: String, val navn: String)
}