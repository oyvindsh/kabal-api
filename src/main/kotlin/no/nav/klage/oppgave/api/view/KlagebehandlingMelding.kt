package no.nav.klage.oppgave.api.view

import java.time.LocalDateTime
import java.util.*

data class MeldingInput(
    val text: String
)

data class MeldingCreated(
    val created: LocalDateTime
)

data class MeldingModified(
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