package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.SmartdokumentUnderArbeidAsVedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface SmartdokumentUnderArbeidAsVedleggRepository : JpaRepository<SmartdokumentUnderArbeidAsVedlegg, UUID> {

    fun findByMarkertFerdigIsNullAndSmartEditorIdNotNull(): List<SmartdokumentUnderArbeidAsVedlegg>

    fun findByParentId(dokumentId: UUID): Set<SmartdokumentUnderArbeidAsVedlegg>

    fun findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull(): List<SmartdokumentUnderArbeidAsVedlegg>

    fun findByMarkertFerdigNotNullAndFerdigstiltNotNullAndParentIdIsNullAndBehandlingId(behandlingId: UUID): SortedSet<SmartdokumentUnderArbeidAsVedlegg>

    fun findByBehandlingIdAndMarkertFerdigIsNullOrderByCreated(behandlingId: UUID): SortedSet<SmartdokumentUnderArbeidAsVedlegg>
}