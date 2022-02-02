package no.nav.klage.dokument.domain.dokumenterunderarbeid

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
    id: DokumentId = DokumentId(UUID.randomUUID()),
    persistentDokumentId: PersistentDokumentId = PersistentDokumentId(UUID.randomUUID()),
    mellomlagerId: String,
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
    persistentDokumentId = persistentDokumentId,
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
            persistentDokumentId = persistentDokumentId,
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

    fun findDokumentUnderArbeidByPersistentDokumentId(persistentDokumentId: PersistentDokumentId): DokumentUnderArbeid? {
        return (listOf(this) + this.vedlegg).find { it.persistentDokumentId == persistentDokumentId }
    }
}