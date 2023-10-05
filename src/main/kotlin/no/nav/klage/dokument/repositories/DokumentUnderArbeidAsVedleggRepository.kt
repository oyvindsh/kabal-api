package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeidAsVedlegg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface DokumentUnderArbeidAsVedleggRepository : JpaRepository<DokumentUnderArbeidAsVedlegg, UUID> {

    fun findByParentId(parentId: UUID): Set<DokumentUnderArbeidAsVedlegg>
}