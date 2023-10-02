package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.SmartdokumentUnderArbeid
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface SmartdokumentUnderArbeidRepository : JpaRepository<SmartdokumentUnderArbeid, UUID> {

    fun findByBehandlingIdAndSmartEditorIdNotNullAndMarkertFerdigIsNullOrderByCreated(behandlingId: UUID): SortedSet<SmartdokumentUnderArbeid>

    fun findByMarkertFerdigIsNullAndSmartEditorIdNotNull(): List<SmartdokumentUnderArbeid>
}