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
@DiscriminatorValue("opplastetdokument")
class OpplastetDokumentUnderArbeid(
    @Column(name = "size")
    var size: Long?,
    @Column(name = "dokument_type_id")
    @Convert(converter = DokumentTypeConverter::class)
    var dokumentType: DokumentType?,

    @Column(name = "dokument_enhet_id")
    var dokumentEnhetId: UUID? = null,
    @ElementCollection
    @CollectionTable(
        schema = "klage",
        name = "dokument_under_arbeid_brevmottaker_ident",
        joinColumns = [JoinColumn(name = "dokument_under_arbeid_id", referencedColumnName = "id", nullable = false)]
    )
    @Column(name="identifikator")
    var brevmottakerIdents: Set<String> = setOf(),
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dokument_under_arbeid_id", referencedColumnName = "id", nullable = false)
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 5)
    val journalposter: MutableSet<DokumentUnderArbeidJournalpostId> = mutableSetOf(),

    //Common properties
    id: UUID = UUID.randomUUID(),
    mellomlagerId: String?,
    name: String,
    behandlingId: UUID,
    created: LocalDateTime,
    modified: LocalDateTime,
    markertFerdig: LocalDateTime?,
    markertFerdigBy: String?,
    ferdigstilt: LocalDateTime?,
    parentId: UUID?,
    creatorIdent: String,
    creatorRole: BehandlingRole,
) : DokumentUnderArbeid(
    id = id,
    mellomlagerId = mellomlagerId,
    name = name,
    behandlingId = behandlingId,
    created = created,
    modified = modified,
    markertFerdig = markertFerdig,
    markertFerdigBy = markertFerdigBy,
    ferdigstilt = ferdigstilt,
    parentId = parentId,
    creatorIdent = creatorIdent,
    creatorRole = creatorRole,
)