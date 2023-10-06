package no.nav.klage.dokument.repositories

import no.nav.klage.dokument.domain.dokumenterunderarbeid.OpplastetDokumentUnderArbeidAsHoveddokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.OpplastetDokumentUnderArbeidAsVedlegg
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.BehandlingRole.KABAL_SAKSBEHANDLING
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
    lateinit var opplastetDokumentUnderArbeidAsHoveddokumentRepository: DokumentUnderArbeidAsHoveddokumentRepository

    @Autowired
    lateinit var opplastetDokumentUnderArbeidAsVedleggRepository: OpplastetDokumentUnderArbeidAsVedleggRepository

    @Test
    fun `persist hoveddokument works`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = OpplastetDokumentUnderArbeidAsHoveddokument(
            mellomlagerId = UUID.randomUUID().toString(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
        )
        hovedDokument.markerFerdigHvisIkkeAlleredeMarkertFerdig(LocalDateTime.now(), "S123456")
        hovedDokument.ferdigstillHvisIkkeAlleredeFerdigstilt(LocalDateTime.now())
        dokumentUnderArbeidRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val byId = dokumentUnderArbeidRepository.getReferenceById(hovedDokument.id)
        assertThat(byId).isEqualTo(hovedDokument)
    }


    @Test
    fun `hoveddokument can have vedlegg`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = OpplastetDokumentUnderArbeidAsHoveddokument(
            mellomlagerId = UUID.randomUUID().toString(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
        )
        dokumentUnderArbeidRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        dokumentUnderArbeidRepository.save(
            OpplastetDokumentUnderArbeidAsVedlegg(
                mellomlagerId = UUID.randomUUID().toString(),
                size = 1001,
                name = "Vedtak.pdf",
                behandlingId = behandlingId,
                dokumentType = DokumentType.BREV,
                parentId = hovedDokument.id,
                creatorIdent = "null",
                creatorRole = KABAL_SAKSBEHANDLING,
                created = LocalDateTime.now(),
                modified = LocalDateTime.now(),
            )
        )

        testEntityManager.flush()
        testEntityManager.clear()

        val vedlegg = opplastetDokumentUnderArbeidAsVedleggRepository.findByParentIdOrderByCreated(hovedDokument.id)
        assertThat(vedlegg).hasSize(1)
    }
/*
    @Test
    fun `vedlegg can be unlinked`() {

        val behandlingId = UUID.randomUUID()
        val hovedDokument = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            smartEditorId = null,
            smartEditorTemplateId = null,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )
        dokumentUnderArbeidRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()


        dokumentUnderArbeidRepository.save(
            DokumentUnderArbeid(
                mellomlagerId = UUID.randomUUID().toString(),
                opplastet = LocalDateTime.now(),
                size = 1001,
                name = "Vedtak.pdf",
                behandlingId = behandlingId,
                dokumentType = DokumentType.BREV,
                smartEditorId = null,
                smartEditorTemplateId = null,
                parentId = hovedDokument.id,
                journalfoertDokumentReference = null,
                creatorIdent = "null",
                creatorRole = KABAL_SAKSBEHANDLING,
            )
        )

        testEntityManager.flush()
        testEntityManager.clear()

        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokument.id).first()
        vedlegg.parentId = null
        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokument.id)).hasSize(0)
    }

    @Test
    fun `documents can be found and edited`() {

        val behandlingId = UUID.randomUUID()
        val nyMellomlagerId = UUID.randomUUID().toString()

        val hovedDokument = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            smartEditorId = null,
            smartEditorTemplateId = null,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )
        dokumentUnderArbeidRepository.save(hovedDokument)

        testEntityManager.flush()
        testEntityManager.clear()

        val hovedDokumentet = dokumentUnderArbeidRepository.getReferenceById(hovedDokument.id)
        assertThat(hovedDokumentet).isNotNull
        hovedDokumentet.mellomlagerId = nyMellomlagerId

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(dokumentUnderArbeidRepository.getReferenceById(hovedDokument.id).mellomlagerId).isEqualTo(nyMellomlagerId)
    }


    @Test
    fun `documents are sorted correctly`() {

        val behandlingId = UUID.randomUUID()

        val hovedDokument1 = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(1),
            smartEditorId = null,
            smartEditorTemplateId = null,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )
        val vedlegg1 = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(2),
            smartEditorId = null,
            smartEditorTemplateId = null,
            parentId = hovedDokument1.id,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )
        val vedlegg2 = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(5),
            smartEditorId = null,
            smartEditorTemplateId = null,
            parentId = hovedDokument1.id,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )

        val hovedDokument2 = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().minusDays(3),
            smartEditorId = null,
            smartEditorTemplateId = null,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )

        val hovedDokument3 = DokumentUnderArbeid(
            mellomlagerId = UUID.randomUUID().toString(),
            opplastet = LocalDateTime.now(),
            size = 1001,
            name = "Vedtak.pdf",
            behandlingId = behandlingId,
            dokumentType = DokumentType.BREV,
            created = LocalDateTime.now().plusDays(3),
            smartEditorId = null,
            smartEditorTemplateId = null,
            journalfoertDokumentReference = null,
            creatorIdent = "null",
            creatorRole = KABAL_SAKSBEHANDLING,
        )
        dokumentUnderArbeidRepository.save(hovedDokument1)
        dokumentUnderArbeidRepository.save(vedlegg1)
        dokumentUnderArbeidRepository.save(vedlegg2)
        dokumentUnderArbeidRepository.save(hovedDokument2)
        dokumentUnderArbeidRepository.save(hovedDokument3)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(dokumentUnderArbeidRepository.findByBehandlingIdAndFerdigstiltIsNullOrderByCreatedDesc(behandlingId)).containsExactly(
            hovedDokument3,
            hovedDokument1,
            vedlegg1,
            hovedDokument2,
            vedlegg2,
        )
    }
*/
}