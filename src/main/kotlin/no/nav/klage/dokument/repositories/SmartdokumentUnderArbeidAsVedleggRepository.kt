package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.SmartdokumentUnderArbeidAsVedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface SmartdokumentUnderArbeidAsVedleggRepository : JpaRepository<SmartdokumentUnderArbeidAsVedlegg, UUID> {

    fun findByBehandlingIdAndSmartEditorIdNotNullAndMarkertFerdigIsNullOrderByCreated(behandlingId: UUID): SortedSet<SmartdokumentUnderArbeidAsVedlegg>

    fun findByMarkertFerdigIsNullAndSmartEditorIdNotNull(): List<SmartdokumentUnderArbeidAsVedlegg>

    fun findByParentIdOrderByCreated(dokumentId: UUID): SortedSet<SmartdokumentUnderArbeidAsVedlegg>

    fun findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull(): List<SmartdokumentUnderArbeidAsVedlegg>

    fun findByMarkertFerdigNotNullAndFerdigstiltNotNullAndParentIdIsNullAndBehandlingId(behandlingId: UUID): SortedSet<SmartdokumentUnderArbeidAsVedlegg>
}