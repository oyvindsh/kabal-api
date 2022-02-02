package no.nav.klage.dokument.api.controller

import org.springframework.web.multipart.MultipartFile

data class FilInput(
    val file: MultipartFile
)