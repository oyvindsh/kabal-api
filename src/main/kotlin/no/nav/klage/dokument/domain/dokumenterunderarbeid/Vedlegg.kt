package no.nav.klage.dokument.domain.dokumenterunderarbeid

import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue(DokumentUnderArbeid.VEDLEGG)
@DynamicUpdate
open class Vedlegg(
    id: DokumentId = DokumentId(UUID.randomUUID()),
    persistentDokumentId: PersistentDokumentId = PersistentDokumentId(UUID.randomUUID()),
    mellomlagerId: String,
    opplastet: LocalDateTime,
    size: Long,
    name: String,
    smartEditorId: UUID? = null,
    behandlingId: UUID,
    dokumentType: DokumentType,
    created: LocalDateTime = LocalDateTime.now(),
    modified: LocalDateTime = LocalDateTime.now(),
) : DokumentUnderArbeid(
    id = id,
    persistentDokumentId = persistentDokumentId,
    mellomlagerId = mellomlagerId,
    opplastet = opplastet,
    size = size,
    name = name,
    smartEditorId = smartEditorId,
    behandlingId = behandlingId,
    dokumentType = dokumentType,
    created = created,
    modified = modified,
) {
    fun toHovedDokument(): HovedDokument =
        HovedDokument(
            persistentDokumentId = persistentDokumentId,
            mellomlagerId = mellomlagerId,
            opplastet = opplastet,
            size = size,
            name = name,
            smartEditorId = smartEditorId,
            behandlingId = behandlingId,
            dokumentType = dokumentType,
            vedlegg = sortedSetOf(),
        )

    fun toDokumentMedParentReferanse(parentId: PersistentDokumentId): DokumentMedParentReferanse {
        return DokumentMedParentReferanse(
            id = id,
            persistentDokumentId = persistentDokumentId,
            mellomlagerId = mellomlagerId,
            opplastet = opplastet,
            size = size,
            name = name,
            smartEditorId = smartEditorId,
            behandlingId = behandlingId,
            dokumentType = dokumentType,
            created = created,
            modified = modified,
            markertFerdig = markertFerdig,
            ferdigstilt = ferdigstilt,
            dokumentEnhetId = null,
            parentId = parentId,
        )
    }
}