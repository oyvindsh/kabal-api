package no.nav.klage.dokument.domain

data class PDFDocument(
    val filename: String,
    val bytes: ByteArray,
)
