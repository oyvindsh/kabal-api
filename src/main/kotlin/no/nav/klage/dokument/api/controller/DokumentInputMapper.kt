package no.nav.klage.dokument.api.controller

import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.OpplastetMellomlagretDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentType
import org.apache.tika.Tika
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class DokumentInputMapper {
    fun mapToMellomlagretDokument(
        multipartFile: MultipartFile,
        dokumentType: DokumentType
    ): MellomlagretDokument {
        return OpplastetMellomlagretDokument(
            title = multipartFile.originalFilename ?: titleFromDokumentType(dokumentType),
            content = multipartFile.bytes,
            contentType = multipartFile.contentType?.let { MediaType.parseMediaType(it) }
                ?: MediaType.valueOf(Tika().detect(multipartFile.bytes))
        )
    }

    private fun titleFromDokumentType(dokumentType: DokumentType): String {
        return dokumentType.navn
    }

}