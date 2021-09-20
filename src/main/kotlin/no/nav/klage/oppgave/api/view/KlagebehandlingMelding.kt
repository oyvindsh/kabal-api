package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime
import java.util.*

data class MeldingInput(
    val text: String,
    val klagebehandlingVersion: Long
)

data class MeldingDeleteInput(
    val klagebehandlingVersion: Long
)

data class MeldingCreated(
    val klagebehandlingVersion: Long,
    val created: LocalDateTime
)

data class MeldingDeleted(
    val klagebehandlingVersion: Long
)

data class MeldingModified(
    val klagebehandlingVersion: Long,
    val modified: LocalDateTime
)

data class MeldingView(
    val id: UUID,
    val text: String,
    val author: Author,
    val created: LocalDateTime,
    val modified: LocalDateTime?
) {
    data class Author(
        val saksbehandlerIdent: String,
        val name: String
    )
}