package no.nav.klage.dokument.api.mapper

import no.nav.klage.dokument.api.view.DokumentView
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentMedParentReferanse
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

    fun mapToDokumentView(dokumentMedParentReferanse: DokumentMedParentReferanse): DokumentView {
        return DokumentView(
            id = dokumentMedParentReferanse.persistentDokumentId.persistentDokumentId,
            tittel = dokumentMedParentReferanse.name,
            dokumentTypeId = dokumentMedParentReferanse.dokumentType.id,
            opplastet = dokumentMedParentReferanse.opplastet,
            isSmartDokument = dokumentMedParentReferanse.smartEditorId != null,
            isMarkertAvsluttet = dokumentMedParentReferanse.markertFerdig != null,
            parent = dokumentMedParentReferanse.parentId?.persistentDokumentId,
        )
    }
}