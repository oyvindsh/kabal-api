package no.nav.klage.dokument.domain.dokumenterunderarbeid

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.oppgave.domain.klage.BehandlingRole
import java.time.LocalDateTime
import java.util.*


@Entity
@DiscriminatorValue("opplastetdokument")
class OpplastetDokumentUnderArbeidAsHoveddokument(
    @Column(name = "size")
    var size: Long?,
    @Column(name = "mellomlager_id")
    override var mellomlagerId: String?,

    //Common properties
    id: UUID = UUID.randomUUID(),
    name: String,
    behandlingId: UUID,
    created: LocalDateTime,
    modified: LocalDateTime,
    markertFerdig: LocalDateTime? = null,
    markertFerdigBy: String? = null,
    ferdigstilt: LocalDateTime? = null,
    creatorIdent: String,
    creatorRole: BehandlingRole,
    dokumentType: DokumentType?,
    dokumentEnhetId: UUID? = null,
    brevmottakerIdents: Set<String> = emptySet(),
    journalposter: Set<DokumentUnderArbeidJournalpostId> = emptySet(),
) : DokumentUnderArbeidAsMellomlagret, DokumentUnderArbeidAsHoveddokument(
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
    dokumentType = dokumentType,
    dokumentEnhetId = dokumentEnhetId,
    brevmottakerIdents = brevmottakerIdents,
    journalposter = journalposter,
){
    fun asVedlegg(parentId: UUID): OpplastetDokumentUnderArbeidAsVedlegg {
        return OpplastetDokumentUnderArbeidAsVedlegg(
            size = size,
            mellomlagerId = mellomlagerId,
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
            parentId = parentId,
            dokumentType = dokumentType,
        )
    }
}