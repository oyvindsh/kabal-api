package no.nav.klage.dokument.api.view

import no.nav.klage.kodeverk.DokumentType
import org.springframework.web.multipart.MultipartFile

data class FilInput(
    val file: MultipartFile,
    val dokumentTypeId: String = DokumentType.NOTAT.id,
)