package no.nav.klage.dokument.domain.dokumenterunderarbeid

import jakarta.persistence.*
import no.nav.klage.kodeverk.Brevmottakertype
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.oppgave.domain.klage.BrevmottakertypeConverter
import no.nav.klage.oppgave.domain.klage.DokumentTypeConverter
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "dokument_under_arbeid", schema = "klage")
@DynamicUpdate
open class DokumentUnderArbeid(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "mellomlager_id")
    open var mellomlagerId: String?,
    @Column(name = "opplastet")
    open var opplastet: LocalDateTime?,
    @Column(name = "size")
    open var size: Long?,
    @Column(name = "name")
    open var name: String,
    @Column(name = "smarteditor_id")
    open var smartEditorId: UUID?,
    @Column(name = "smarteditor_template_id")
    open var smartEditorTemplateId: String?,
    @Column(name = "smarteditor_version")
    open var smartEditorVersion: Int?,
    @Column(name = "behandling_id")
    open var behandlingId: UUID,
    @Column(name = "dokument_type")
    @Convert(converter = DokumentTypeConverter::class)
    open var dokumentType: DokumentType,
    @Column(name = "created")
    open var created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    open var modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "markert_ferdig")
    open var markertFerdig: LocalDateTime? = null,
    @Column(name = "markert_ferdig_by")
    open var markertFerdigBy: String? = null,
    @Column(name = "ferdigstilt")
    open var ferdigstilt: LocalDateTime? = null,
    @Column(name = "dokument_enhet_id")
    open var dokumentEnhetId: UUID? = null,
    @ElementCollection(targetClass = Brevmottakertype::class, fetch = FetchType.EAGER)
    @CollectionTable(
        name = "dokument_under_arbeid_brevmottaker_type",
        schema = "klage",
        joinColumns = [JoinColumn(name = "dokument_under_arbeid_id", referencedColumnName = "id", nullable = true)]
    )
    @Convert(converter = BrevmottakertypeConverter::class)
    @Column(name = "id")
    open var brevmottakertyper: MutableSet<Brevmottakertype> = mutableSetOf(),
    @Column(name = "parent_id")
    open var parentId: UUID? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dokument_under_arbeid_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 5)
    open val journalposter: MutableSet<DokumentUnderArbeidJournalpostId> = mutableSetOf(),
) : Comparable<DokumentUnderArbeid> {

    override fun compareTo(other: DokumentUnderArbeid): Int =
        created.compareTo(other.created)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DokumentUnderArbeid

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "DokumentUnderArbeid(id=$id)"
    }

    fun erMarkertFerdig(): Boolean {
        return markertFerdig != null
    }

    fun erFerdigstilt(): Boolean {
        return ferdigstilt != null
    }

    fun ferdigstillHvisIkkeAlleredeFerdigstilt(tidspunkt: LocalDateTime) {
        if (ferdigstilt == null) {
            ferdigstilt = tidspunkt
            modified = tidspunkt
        }
    }

    fun markerFerdigHvisIkkeAlleredeMarkertFerdig(tidspunkt: LocalDateTime, saksbehandlerIdent: String) {
        if (markertFerdig == null) {
            markertFerdig = tidspunkt
            markertFerdigBy = saksbehandlerIdent
            modified = tidspunkt
        }
    }

}