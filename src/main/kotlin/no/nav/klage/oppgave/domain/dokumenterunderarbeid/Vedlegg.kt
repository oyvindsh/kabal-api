package no.nav.klage.oppgave.domain.dokumenterunderarbeid

import java.time.LocalDateTime
import java.util.*
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

@Entity
@DiscriminatorValue(DokumentUnderArbeid.VEDLEGG)
open class Vedlegg(
    id: UUID = UUID.randomUUID(),
    dokumentId: UUID = UUID.randomUUID(),
    mellomlagerId: UUID,
    opplastet: LocalDateTime,
    size: Long,
    name: String,
    smartEditorId: UUID? = null,
    behandlingId: UUID,
    dokumentType: DokumentType,
) : DokumentUnderArbeid(
    id = id,
    dokumentId = dokumentId,
    mellomlagerId = mellomlagerId,
    opplastet = opplastet,
    size = size,
    name = name,
    smartEditorId = smartEditorId,
    behandlingId = behandlingId,
    dokumentType = dokumentType
) {
    fun toHovedDokument(): HovedDokument =
        HovedDokument(
            dokumentId = dokumentId,
            mellomlagerId = mellomlagerId,
            opplastet = opplastet,
            size = size,
            name = name,
            smartEditorId = smartEditorId,
            behandlingId = behandlingId,
            dokumentType = dokumentType,
            vedlegg = mutableListOf(),
        )

}