package no.nav.klage.dokument.domain.dokumenterunderarbeid

import jakarta.persistence.*
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.kodeverk.DokumentTypeConverter
import no.nav.klage.oppgave.domain.klage.BehandlingRole
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.LocalDateTime
import java.util.*


@Entity
@DiscriminatorValue("smartdokument")
class SmartdokumentUnderArbeid(
    @Column(name = "size")
    override var size: Long?,
    @Column(name = "smarteditor_id")
    override val smartEditorId: UUID,
    @Column(name = "smarteditor_template_id")
    override var smartEditorTemplateId: String,
    @Column(name = "dokument_type_id")
    @Convert(converter = DokumentTypeConverter::class)
    override var dokumentType: DokumentType,
    @Column(name = "dokument_enhet_id")
    var dokumentEnhetId: UUID? = null,
    @ElementCollection
    @CollectionTable(
        schema = "klage",
        name = "dokument_under_arbeid_brevmottaker_ident",
        joinColumns = [JoinColumn(name = "dokument_under_arbeid_id", referencedColumnName = "id", nullable = false)]
    )
    @Column(name="identifikator")
    override var brevmottakerIdents: Set<String> = setOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dokument_under_arbeid_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 5)
    val journalposter: MutableSet<DokumentUnderArbeidJournalpostId> = mutableSetOf(),
    @Column(name = "mellomlager_id")
    override var mellomlagerId: String?,

    //Common properties
    id: UUID = UUID.randomUUID(),
    name: String,
    behandlingId: UUID,
    created: LocalDateTime,
    modified: LocalDateTime,
    markertFerdig: LocalDateTime?,
    markertFerdigBy: String?,
    ferdigstilt: LocalDateTime?,
    creatorIdent: String,
    creatorRole: BehandlingRole,
) : DokumentUnderArbeidAsSmartdokument, DokumentUnderArbeidWithHoveddokumentCharacteristics, DokumentUnderArbeid(
    id = id,
    name = name,
    behandlingId = behandlingId,
    created = created,
    modified = modified,
    markertFerdig = markertFerdig,
    markertFerdigBy = markertFerdigBy,
    ferdigstilt = ferdigstilt,
    creatorIdent = creatorIdent,
    creatorRole = creatorRole,
)