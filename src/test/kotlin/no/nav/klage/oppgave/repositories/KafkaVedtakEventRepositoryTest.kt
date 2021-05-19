package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.KafkaVedtakEvent
import no.nav.klage.oppgave.domain.kodeverk.Utfall
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

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KafkaVedtakEventRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var kafkaVedtakEventRepository: KafkaVedtakEventRepository

    @Test
    fun `store event works`() {
        val event = KafkaVedtakEvent(
            kildeReferanse = "TEST",
            kilde = "TEST",
            utfall = Utfall.MEDHOLD,
            vedtaksbrevReferanse = "TEST",
            kabalReferanse = "TEST",
            status = UtsendingStatus.IKKE_SENDT
        )

        kafkaVedtakEventRepository.save(event)

        testEntityManager.flush()

        assertThat(kafkaVedtakEventRepository.findById(event.id).get()).isEqualTo(event)
    }

    @Test
    fun `fetch for status works`() {
        kafkaVedtakEventRepository.save(
            KafkaVedtakEvent(
                kildeReferanse = "TEST",
                kilde = "TEST",
                utfall = Utfall.MEDHOLD,
                vedtaksbrevReferanse = "TEST",
                kabalReferanse = "TEST",
                status = UtsendingStatus.IKKE_SENDT
            )
        )

        kafkaVedtakEventRepository.save(
            KafkaVedtakEvent(
                kildeReferanse = "TEST",
                kilde = "TEST",
                utfall = Utfall.MEDHOLD,
                vedtaksbrevReferanse = "TEST",
                kabalReferanse = "TEST",
                status = UtsendingStatus.FEILET
            )
        )

        testEntityManager.flush()

        val list = kafkaVedtakEventRepository.getAllByStatusIsNotLike(UtsendingStatus.SENDT)
        assertThat(list.size).isEqualTo(2)
    }

}
