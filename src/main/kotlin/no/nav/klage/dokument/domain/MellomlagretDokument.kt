package no.nav.klage.dokument.domain

import org.springframework.http.MediaType

interface MellomlagretDokument {
    val title: String
    val content: ByteArray
    val contentType: MediaType
}