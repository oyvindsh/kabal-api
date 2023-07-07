package no.nav.klage.oppgave.clients.saf.rest

import org.springframework.http.MediaType

data class ArkivertDokument(
    val bytes: ByteArray,
    val contentType: MediaType,
    val filename: String,
)
