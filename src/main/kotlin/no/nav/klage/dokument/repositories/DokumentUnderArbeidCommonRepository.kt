package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeidAsHoveddokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeidAsVedlegg
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class DokumentUnderArbeidCommonRepository(
    private val opplastetDokumentUnderArbeidAsVedleggRepository: OpplastetDokumentUnderArbeidAsVedleggRepository,
    private val journalfoertDokumentUnderArbeidAsVedleggRepository: JournalfoertDokumentUnderArbeidAsVedleggRepository,
    private val smartdokumentUnderArbeidAsVedleggRepository: SmartdokumentUnderArbeidAsVedleggRepository,
    private val opplastetDokumentUnderArbeidAsHoveddokumentRepository: OpplastetDokumentUnderArbeidAsHoveddokumentRepository,
    private val smartdokumentUnderArbeidAsHoveddokumentRepository: SmartdokumentUnderArbeidAsHoveddokumentRepository,
) {

    fun findVedleggByParentId(parentId: UUID): Set<DokumentUnderArbeidAsVedlegg> {
        return opplastetDokumentUnderArbeidAsVedleggRepository.findByParentIdOrderByCreated(parentId) +
                journalfoertDokumentUnderArbeidAsVedleggRepository.findByParentIdOrderByCreated(parentId) +
                smartdokumentUnderArbeidAsVedleggRepository.findByParentIdOrderByCreated(parentId)
    }

//    fun findHoveddokumenterByBehandlingId(behandlingId: UUID): Set<DokumentUnderArbeid> {
//        return opplastetDokumentUnderArbeidAsHoveddokumentRepository.findByBehandlingId(behandlingId) +
//                smartdokumentUnderArbeidAsHoveddokumentRepository.findByBehandlingId(behandlingId)
//    }

    fun findHoveddokumenterByMarkertFerdigNotNullAndFerdigstiltNotNullAndBehandlingId(behandlingId: UUID): Set<DokumentUnderArbeidAsHoveddokument> {
        return opplastetDokumentUnderArbeidAsHoveddokumentRepository.findByBehandlingIdAndMarkertFerdigIsNull(
            behandlingId
        ) + smartdokumentUnderArbeidAsHoveddokumentRepository.findByBehandlingId(behandlingId)
    }

    fun findHoveddokumenterByMarkertFerdigNotNullAndFerdigstiltNull(): Set<DokumentUnderArbeidAsHoveddokument> {
        return opplastetDokumentUnderArbeidAsHoveddokumentRepository.findByMarkertFerdigNotNullAndFerdigstiltNull() +
                smartdokumentUnderArbeidAsHoveddokumentRepository.findByMarkertFerdigNotNullAndFerdigstiltNull()
    }

}