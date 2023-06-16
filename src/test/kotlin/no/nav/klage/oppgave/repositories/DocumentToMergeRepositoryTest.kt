package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentToMergeRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var documentToMergeRepository: DocumentToMergeRepository

    @Test
    fun `delete old merged documents works`() {
        val thresholdWeeks = 3L

        val now = LocalDateTime.of(LocalDate.of(2023, 5, 1), LocalTime.MIN)

        val docsToMerge = mutableListOf<DocumentToMerge>()

        val referenceIdToKeep = UUID.randomUUID()
        docsToMerge += DocumentToMerge(
            referenceId = referenceIdToKeep,
            journalpostId = "1",
            dokumentInfoId = "1",
            index = 0,
            created = now.minusWeeks(thresholdWeeks),
        )

        docsToMerge += DocumentToMerge(
            referenceId = UUID.randomUUID(),
            journalpostId = "2",
            dokumentInfoId = "2",
            index = 0,
            created = now.minusWeeks(thresholdWeeks).minusMinutes(1),
        )

        documentToMergeRepository.saveAll(docsToMerge)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(documentToMergeRepository.findAll()).hasSize(2)

        documentToMergeRepository.deleteByCreatedBefore(now.minusWeeks(thresholdWeeks))

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(documentToMergeRepository.findAll()).hasSize(1)
        assertThat(documentToMergeRepository.findAll().first().referenceId).isEqualTo(referenceIdToKeep)
    }

}