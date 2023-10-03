package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.SmartdokumentUnderArbeidAsHoveddokument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface SmartdokumentUnderArbeidRepository : JpaRepository<SmartdokumentUnderArbeidAsHoveddokument, UUID> {

    fun findByBehandlingIdAndSmartEditorIdNotNullAndMarkertFerdigIsNullOrderByCreated(behandlingId: UUID): SortedSet<SmartdokumentUnderArbeidAsHoveddokument>

    fun findByMarkertFerdigIsNullAndSmartEditorIdNotNull(): List<SmartdokumentUnderArbeidAsHoveddokument>
}