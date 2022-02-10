package no.nav.klage.dokument.domain.dokumenterunderarbeid

import java.time.LocalDateTime
import java.util.*

data class DokumentMedParentReferanse(
    val id: DokumentId,
    val persistentDokumentId: PersistentDokumentId,
    val mellomlagerId: String,
    val opplastet: LocalDateTime,
    val size: Long,
    val name: String,
    val smartEditorId: UUID?,
    val behandlingId: UUID,
    val dokumentType: DokumentType,
    val created: LocalDateTime,
    val modified: LocalDateTime,
    val markertFerdig: LocalDateTime?,
    val ferdigstilt: LocalDateTime?,
    val dokumentEnhetId: UUID?,
    val parentId: PersistentDokumentId?,
) : Comparable<DokumentMedParentReferanse> {

    override fun compareTo(other: DokumentMedParentReferanse): Int =
        opplastet.compareTo(other.opplastet)
}