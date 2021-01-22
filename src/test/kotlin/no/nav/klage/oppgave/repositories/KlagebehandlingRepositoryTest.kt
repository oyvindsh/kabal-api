package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
class KlagebehandlingRepositoryTest {

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Test
    fun `persist klage works`() {
        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            sakstype = Sakstype(1, "", ""),
            hjemler = listOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            oppgavereferanser = listOf(),
            modified = LocalDateTime.now(),
            created = LocalDateTime.now(),
            frist = LocalDate.now()
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(klagebehandlingRepository.findById(klage.id).get()).isEqualTo(klage)
    }
}
