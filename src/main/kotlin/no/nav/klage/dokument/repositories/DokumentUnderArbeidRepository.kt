package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface DokumentUnderArbeidRepository : JpaRepository<DokumentUnderArbeid, UUID> {

    fun findByBehandlingId(behandlingId: UUID): Set<DokumentUnderArbeid>

    fun findByBehandlingIdAndFerdigstiltIsNullOrderByCreated(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByBehandlingIdAndMarkertFerdigIsNull(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByBehandlingIdAndSmartEditorIdNotNullAndMarkertFerdigIsNullOrderByCreated(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByParentIdOrderByCreated(dokumentId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull(): List<DokumentUnderArbeid>

    fun findByMarkertFerdigNotNullAndFerdigstiltNotNullAndParentIdIsNullAndBehandlingId(behandlingId: UUID): SortedSet<DokumentUnderArbeid>
}