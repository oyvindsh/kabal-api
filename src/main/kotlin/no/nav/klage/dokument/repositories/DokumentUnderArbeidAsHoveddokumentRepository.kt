package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeidAsHoveddokument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface DokumentUnderArbeidAsHoveddokumentRepository : JpaRepository<DokumentUnderArbeidAsHoveddokument, UUID> {

    fun findByMarkertFerdigNotNullAndFerdigstiltNotNullAndBehandlingId(behandlingId: UUID): List<DokumentUnderArbeidAsHoveddokument>
}