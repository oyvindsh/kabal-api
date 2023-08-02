package no.nav.klage.oppgave.domain.kafka

/**
 * SSE for subscribing clients
 */
data class Event(
    val behandlingId: String,
    val name: String,
    val id: String,
    val data: String,
)