package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.dokument.domain.dokumenterunderarbeid.JournalfoertDokumentReference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface DokumentUnderArbeidRepository : JpaRepository<DokumentUnderArbeid, UUID> {

    fun findByBehandlingId(behandlingId: UUID): Set<DokumentUnderArbeid>

    fun findByBehandlingIdAndFerdigstiltIsNullOrderByCreatedDesc(behandlingId: UUID): List<DokumentUnderArbeid>

    fun findByBehandlingIdAndMarkertFerdigIsNull(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByBehandlingIdAndSmartEditorIdNotNullAndMarkertFerdigIsNullOrderByCreated(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByParentIdOrderByCreated(dokumentId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByParentIdAndJournalfoertDokumentReferenceIsNotNull(dokumentId: UUID): Set<DokumentUnderArbeid>

    fun findByParentIdAndJournalfoertDokumentReferenceAndIdNot(parentId: UUID, journalfoertDokumentReference: JournalfoertDokumentReference, id: UUID): List<DokumentUnderArbeid>

    fun findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull(): List<DokumentUnderArbeid>

    fun findByMarkertFerdigNotNullAndFerdigstiltNotNullAndParentIdIsNullAndBehandlingId(behandlingId: UUID): SortedSet<DokumentUnderArbeid>
}