package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.dokumenterunderarbeid.DokumentType
import no.nav.klage.oppgave.domain.dokumenterunderarbeid.HovedDokument
import no.nav.klage.oppgave.domain.dokumenterunderarbeid.Vedlegg
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

    @Autowired
    lateinit var dokumentUnderArbeidRepository: DokumentUnderArbeidRepository

    @Autowired
    lateinit var hovedDokumentRepository: HovedDokumentRepository

    @Autowired
    lateinit var vedleggRepository: VedleggRepository

    @Test
    fun `persist hoveddokument works`() {

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

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId).isEqualTo(hovedDokument)
    }

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

    @Test
    fun `hoveddokument can have vedlegg`() {

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

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        byId.vedlegg.add(
            Vedlegg(
                mellomlagerId = UUID.randomUUID(),
                opplastet = LocalDateTime.now(),
                size = 1001,
                name = "Vedtak.pdf",
                behandlingId = behandlingId,
                dokumentType = DokumentType.VEDTAK,
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

        val byId = hovedDokumentRepository.getById(hovedDokument.id)
        val vedlegg = Vedlegg(
            mellomlagerId = UUID.randomUUID(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.VEDTAK,
        )
        byId.vedlegg.add(vedlegg)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId2 = hovedDokumentRepository.findByDokumentIdOrVedleggDokumentId(vedlegg.dokumentId)
        assertThat(byId2).isEqualTo(hovedDokument)
        assertThat(byId2.vedlegg).hasSize(1)
        assertThat(byId2.vedlegg.first()).isEqualTo(vedlegg)
    }

    @Test
    fun `vedlegg can be unlinked`() {

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
        byId.vedlegg.add(
            Vedlegg(
                mellomlagerId = UUID.randomUUID(),
                opplastet = LocalDateTime.now(),
                size = 1001,
                name = "Vedtak.pdf",
                behandlingId = behandlingId,
                dokumentType = DokumentType.VEDTAK,
            )
        )

        testEntityManager.flush()
        testEntityManager.clear()

        val byId2 = hovedDokumentRepository.getById(hovedDokument.id)
        val hovedDokumentFraTidligereVedlegg = byId2.vedlegg.removeAt(0).toHovedDokument()
        hovedDokumentRepository.save(hovedDokumentFraTidligereVedlegg)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId3 = hovedDokumentRepository.getById(hovedDokument.id)
        assertThat(byId3.vedlegg).hasSize(0)

        val byId4 = hovedDokumentRepository.getById(hovedDokumentFraTidligereVedlegg.id)
        assertThat(byId4).isEqualTo(hovedDokumentFraTidligereVedlegg)
    }
}