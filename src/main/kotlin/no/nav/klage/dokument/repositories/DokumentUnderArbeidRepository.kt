package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentId
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface DokumentUnderArbeidRepository : JpaRepository<DokumentUnderArbeid, DokumentId> {

    fun findByBehandlingIdAndFerdigstiltIsNullOrderByCreated(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByBehandlingIdAndMarkertFerdigIsNotNull(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByBehandlingIdAndSmartEditorIdNotNullOrderByCreated(behandlingId: UUID): SortedSet<DokumentUnderArbeid>

    fun findByParentIdOrderByCreated(dokumentId: DokumentId): SortedSet<DokumentUnderArbeid>

    fun findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull(): List<DokumentUnderArbeid>

    fun findByMarkertFerdigNotNullAndFerdigstiltNotNullAndParentIdIsNullAndBehandlingId(behandlingId: UUID): SortedSet<DokumentUnderArbeid>
}