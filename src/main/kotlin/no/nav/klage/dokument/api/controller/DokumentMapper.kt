package no.nav.klage.dokument.api.controller

import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.HovedDokument
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class DokumentMapper {

    fun mapToByteArray(mellomlagretDokument: MellomlagretDokument): ResponseEntity<ByteArray> =
        ResponseEntity(
            mellomlagretDokument.content,
            HttpHeaders().apply {
                contentType = mellomlagretDokument.contentType
                add("Content-Disposition", "inline; filename=${mellomlagretDokument.title}")
            },
            HttpStatus.OK
        )

    fun mapToHovedDokumentView(hovedDokument: HovedDokument): HovedDokumentView {
        return HovedDokumentView(hovedDokument.dokumentType.id) //TODO
    }


}