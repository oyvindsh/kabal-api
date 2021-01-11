package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Klagesak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
class KlagesakRepositoryTest {

    @Autowired
    lateinit var klagesakRepository: KlagesakRepository

    @Test
    fun `persist klage works`() {
        val klage = Klagesak(
            foedselsnummer = "12345678910",
            sakstypeId = 1,
            modified = LocalDateTime.now(),
            created = LocalDateTime.now()
        )

        klagesakRepository.save(klage)

        assertThat(klagesakRepository.findById(klage.id).get()).isEqualTo(klage)
    }
}
