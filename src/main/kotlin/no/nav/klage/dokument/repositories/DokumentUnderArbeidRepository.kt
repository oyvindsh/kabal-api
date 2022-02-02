package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentId
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.dokument.domain.dokumenterunderarbeid.HovedDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.PersistentDokumentId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*


//@NoRepositoryBean
//interface BaseDokumentUnderArbeidRepository<T : DokumentUnderArbeid> : JpaRepository<T, UUID> {
//@Query("select d from #{#entityName} as d from dokument where d.ekstern_referanse = eksternReferanse")
//}


//@Transactional
//interface DokumentUnderArbeidRepository : BaseDokumentUnderArbeidRepository<DokumentUnderArbeid> {
//}

@Transactional
interface HovedDokumentRepository : //BaseDokumentUnderArbeidRepository<HovedDokument>,
    JpaRepository<HovedDokument, DokumentId> {
    fun findByBehandlingId(behandlingId: UUID): List<HovedDokument>
    fun findByPersistentDokumentId(persistentDokumentId: PersistentDokumentId): HovedDokument?
    fun findByPersistentDokumentIdOrVedleggPersistentDokumentId(
        persistentDokumentId: PersistentDokumentId,
        vedleggPersistentDokumentId: PersistentDokumentId
    ): HovedDokument?

    fun findByVedleggPersistentDokumentId(persistentDokumentId: PersistentDokumentId): HovedDokument?
    fun findByVedleggId(id: DokumentId): HovedDokument?
    fun findByMarkertFerdigNotNullAndFerdigstiltNull(): List<HovedDokument>
}

fun HovedDokumentRepository.findByPersistentDokumentIdOrVedleggPersistentDokumentId(persistentDokumentId: PersistentDokumentId): HovedDokument? =
    this.findByPersistentDokumentIdOrVedleggPersistentDokumentId(persistentDokumentId, persistentDokumentId)

fun HovedDokumentRepository.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(
    persistentDokumentId: PersistentDokumentId
): DokumentUnderArbeid? =
    this.findByPersistentDokumentIdOrVedleggPersistentDokumentId(persistentDokumentId, persistentDokumentId)
        ?.findDokumentUnderArbeidByPersistentDokumentId(persistentDokumentId)

fun HovedDokumentRepository.getDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(
    persistentDokumentId: PersistentDokumentId
): DokumentUnderArbeid =
    findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(persistentDokumentId)!!

//@Transactional
//interface VedleggRepository : BaseDokumentUnderArbeidRepository<Vedlegg>, JpaRepository<Vedlegg, UUID>