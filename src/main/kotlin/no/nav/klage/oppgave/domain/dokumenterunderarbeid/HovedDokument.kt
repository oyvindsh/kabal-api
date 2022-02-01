package no.nav.klage.oppgave.domain.dokumenterunderarbeid

import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@DiscriminatorValue(DokumentUnderArbeid.HOVED_DOKUMENT)
@DynamicUpdate
open class HovedDokument(
    id: UUID = UUID.randomUUID(),
    dokumentId: UUID = UUID.randomUUID(),
    mellomlagerId: UUID,
    opplastet: LocalDateTime,
    size: Long,
    name: String,
    smartEditorId: UUID? = null,
    behandlingId: UUID,
    dokumentType: DokumentType,
    @OneToMany(
        cascade = [CascadeType.ALL],
        targetEntity = Vedlegg::class,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    @JoinColumn(name = "parent_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 100)
    open var vedlegg: MutableList<Vedlegg> = mutableListOf(),
    open var dokumentEnhetId: UUID? = null,
) : DokumentUnderArbeid(
    id = id,
    dokumentId = dokumentId,
    mellomlagerId = mellomlagerId,
    opplastet = opplastet,
    size = size,
    name = name,
    smartEditorId = smartEditorId,
    behandlingId = behandlingId,
    dokumentType = dokumentType,
) {
    fun toVedlegg(): Vedlegg =
        Vedlegg(
            dokumentId = dokumentId,
            mellomlagerId = mellomlagerId,
            opplastet = opplastet,
            size = size,
            name = name,
            smartEditorId = smartEditorId,
            behandlingId = behandlingId,
            dokumentType = dokumentType,
        )

    fun ferdigstillHvisIkkeAlleredeFerdigstilt() {
        val naa = LocalDateTime.now()
        super.ferdigstillHvisIkkeAlleredeFerdigstilt(naa)
        vedlegg.forEach { it.ferdigstillHvisIkkeAlleredeFerdigstilt(naa) }
    }

    fun markerFerdigHvisIkkeAlleredeMarkertFerdig() {
        val naa = LocalDateTime.now()
        super.markerFerdigHvisIkkeAlleredeMarkertFerdig(naa)
        vedlegg.forEach { it.markerFerdigHvisIkkeAlleredeMarkertFerdig(naa) }
    }

    fun findDokumentUnderArbeidByDokumentId(dokumentId: UUID): DokumentUnderArbeid? {
        return (listOf(this) + this.vedlegg).find { it.dokumentId == dokumentId }
    }
}