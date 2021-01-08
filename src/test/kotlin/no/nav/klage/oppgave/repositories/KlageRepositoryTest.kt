package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Klagesak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate

@ActiveProfiles("local")
@DataJpaTest
class KlageRepositoryTest {

    @Autowired
    lateinit var klageRepository: KlageRepository

    @Test
    fun `persist klage works`() {
        val klage = Klagesak(
            foedselsnummer = "12345678910",
            datoMottattFraFoersteinstans = LocalDate.now(),
            frist = LocalDate.now()
        )

        klageRepository.save(klage)

        assertThat(klageRepository.findById(klage.id).get()).isEqualTo(klage)
    }
}
