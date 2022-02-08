package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentType
import no.nav.klage.dokument.domain.dokumenterunderarbeid.HovedDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.Vedlegg
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DokumentUnderArbeidRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    //@Autowired
    //lateinit var dokumentUnderArbeidRepository: DokumentUnderArbeidRepository

    @Autowired
    lateinit var hovedDokumentRepository: HovedDokumentRepository

    //@Autowired
    //lateinit var vedleggRepository: VedleggRepository

    @Test
    fun `persist hoveddokument works`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokument.markerFerdigHvisIkkeAlleredeMarkertFerdig()
        hovedDokument.ferdigstillHvisIkkeAlleredeFerdigstilt()
        hovedDokumentRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId).isEqualTo(hovedDokument)
    }

    /*
    @Test
    fun `hoveddokument can be persisted by dokumentUnderArbeidRepository and retrieved by hovedDokumentRepository`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.VEDTAK,
        )
        dokumentUnderArbeidRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId).isEqualTo(hovedDokument)
    }

     */

    /*
    @Test
    fun `hoveddokument can be persisted by hovedDokumentRepository and retrieved by dokumentUnderArbeidRepository`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.VEDTAK,
        )
        hovedDokumentRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = dokumentUnderArbeidRepository.findById(hovedDokument.id).get()
        assertThat(byId).isEqualTo(hovedDokument)
    }
     */

    @Test
    fun `hoveddokument can have vedlegg`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokumentRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        byId.vedlegg.add(
            Vedlegg(
                mellomlagerId = UUID.randomUUID().toString(),
                opplastet = LocalDateTime.now(),
                size = 1001,
                name = "Vedtak.pdf",
                behandlingId = behandlingId,
                dokumentType = DokumentType.BREV,
            )
        )

        testEntityManager.flush()
        testEntityManager.clear()

        val byId2 = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId2.vedlegg).hasSize(1)
    }

    @Test
    fun `HovedDokument can be queried by vedlegg`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokumentRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        val vedlegg = Vedlegg(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        byId.vedlegg.add(vedlegg)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId2 = hovedDokumentRepository.findByVedleggPersistentDokumentId(vedlegg.persistentDokumentId)
        assertThat(byId2).isEqualTo(hovedDokument)
        assertThat(byId2!!.vedlegg).hasSize(1)
        assertThat(byId2.vedlegg.first()).isEqualTo(vedlegg)
    }

    @Test
    fun `vedlegg can be unlinked`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokumentRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        byId.vedlegg.add(
            Vedlegg(
                mellomlagerId = UUID.randomUUID().toString(),
                opplastet = LocalDateTime.now(),
                size = 1001,
                name = "Vedtak.pdf",
                behandlingId = behandlingId,
                dokumentType = DokumentType.BREV,
            )
        )

        testEntityManager.flush()
        testEntityManager.clear()

        val byId2 = hovedDokumentRepository.getById(hovedDokument.id)
        val vedlegg = byId2.vedlegg.first()
        byId2.vedlegg.remove(vedlegg)
        val hovedDokumentFraTidligereVedlegg = vedlegg.toHovedDokument()
        hovedDokumentRepository.save(hovedDokumentFraTidligereVedlegg)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId3 = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId3.vedlegg).hasSize(0)

        val byId4 = hovedDokumentRepository.getById(hovedDokumentFraTidligereVedlegg.id)
        assertThat(byId4).isEqualTo(hovedDokumentFraTidligereVedlegg)
    }

    @Test
    fun `two hoveddokument can be linked`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument1 = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokumentRepository.save(hovedDokument1)

        val hovedDokument2 = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokumentRepository.save(hovedDokument2)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId1 = hovedDokumentRepository.getById(hovedDokument1.id)
        val byId2 = hovedDokumentRepository.getById(hovedDokument2.id)

        hovedDokumentRepository.delete(byId2)
        val nyttVedlegg = byId2.toVedlegg()
        byId1.vedlegg.add(nyttVedlegg)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId3 = hovedDokumentRepository.getById(hovedDokument1.id)
        assertThat(byId3.vedlegg).hasSize(1)
        assertThat(byId3.vedlegg.first().persistentDokumentId).isEqualTo(nyttVedlegg.persistentDokumentId)
    }

    @Test
    fun `documents with unknown type can be found and edited`() {

        val behandlingId = UUID.randomUUID()
        val nyMellomlagerId = UUID.randomUUID().toString()

        val hovedDokument = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        val vedlegg = Vedlegg(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
        )
        hovedDokument.vedlegg.add(vedlegg)
        hovedDokumentRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()


        val hovedDokumentet =
            hovedDokumentRepository.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(
                hovedDokument.persistentDokumentId
            )
        assertThat(hovedDokumentet).isNotNull
        hovedDokumentet!!.mellomlagerId = nyMellomlagerId

        testEntityManager.flush()
        testEntityManager.clear()

        val vedlegget =
            hovedDokumentRepository.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(vedlegg.persistentDokumentId)
        assertThat(vedlegget).isNotNull
        vedlegget!!.mellomlagerId = nyMellomlagerId

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId.mellomlagerId).isEqualTo(nyMellomlagerId)
        assertThat(byId.vedlegg.first().mellomlagerId).isEqualTo(nyMellomlagerId)
    }


    @Test
    fun `documents are sorted correctly`() {

        val behandlingId = UUID.randomUUID()

        val hovedDokument1 = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(1)
        )
        val vedlegg1 = Vedlegg(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(2)
        )
        val vedlegg2 = Vedlegg(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(5)
        )

        val hovedDokument2 = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(3)
        )

        val hovedDokument3 = HovedDokument(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().plusDays(3)
        )
        hovedDokument1.vedlegg.add(vedlegg1)
        hovedDokument1.vedlegg.add(vedlegg2)
        assertThat(hovedDokument1.vedlegg.first()).isEqualTo(vedlegg2)
        assertThat(sortedSetOf(hovedDokument1, hovedDokument2, hovedDokument3)).containsExactly(
            hovedDokument2,
            hovedDokument1,
            hovedDokument3
        )


        hovedDokumentRepository.save(hovedDokument1)
        hovedDokumentRepository.save(hovedDokument2)
        hovedDokumentRepository.save(hovedDokument3)

        assertThat(hovedDokument1.vedlegg.first()).isEqualTo(vedlegg2)
        assertThat(sortedSetOf(hovedDokument1, hovedDokument2, hovedDokument3)).containsExactly(
            hovedDokument2,
            hovedDokument1,
            hovedDokument3
        )

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(hovedDokumentRepository.findByBehandlingIdOrderByCreated(behandlingId)).containsExactly(
            hovedDokument2,
            hovedDokument1,
            hovedDokument3
        )
        assertThat(hovedDokumentRepository.getById(hovedDokument1.id).vedlegg.first()).isEqualTo(vedlegg2)
    }

}