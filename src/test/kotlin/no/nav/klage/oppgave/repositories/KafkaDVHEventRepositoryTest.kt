package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.KafkaDVHEvent
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KafkaDVHEventRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var kafkaDVHEventRepository: KafkaDVHEventRepository

    @Test
    fun `store event works`() {
        val event = KafkaDVHEvent(
            kildeReferanse = "TEST",
            kilde = "TEST",
            klagebehandlingId = UUID.randomUUID(),
            status = UtsendingStatus.IKKE_SENDT,
            jsonPayload = "{}"
        )

        kafkaDVHEventRepository.save(event)

        testEntityManager.flush()

        assertThat(kafkaDVHEventRepository.findById(event.id).get()).isEqualTo(event)
    }

    @Test
    fun `fetch for status works`() {
        kafkaDVHEventRepository.save(
            KafkaDVHEvent(
                kildeReferanse = "TEST",
                kilde = "TEST",
                klagebehandlingId = UUID.randomUUID(),
                status = UtsendingStatus.IKKE_SENDT,
                jsonPayload = "{}"
            )
        )

        kafkaDVHEventRepository.save(
            KafkaDVHEvent(
                kildeReferanse = "TEST",
                kilde = "TEST",
                klagebehandlingId = UUID.randomUUID(),
                status = UtsendingStatus.FEILET,
                jsonPayload = "{}"
            )
        )

        testEntityManager.flush()

        val list = kafkaDVHEventRepository.getAllByStatusIsNotLike(UtsendingStatus.SENDT)
        assertThat(list.size).isEqualTo(2)
    }

}