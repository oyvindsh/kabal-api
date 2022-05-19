package no.nav.klage.dokument.api.mapper

import no.nav.klage.dokument.api.view.DokumentView
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
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

    fun mapToDokumentView(dokumentUnderArbeid: DokumentUnderArbeid): DokumentView {
        return DokumentView(
            id = dokumentUnderArbeid.id.id,
            tittel = dokumentUnderArbeid.name,
            dokumentTypeId = dokumentUnderArbeid.dokumentType.id,
            opplastet = dokumentUnderArbeid.opplastet,
            isSmartDokument = dokumentUnderArbeid.smartEditorId != null,
            templateId = dokumentUnderArbeid.smartEditorTemplateId,
            isMarkertAvsluttet = dokumentUnderArbeid.markertFerdig != null,
            parent = dokumentUnderArbeid.parentId?.id,
        )
    }
}