package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagesak
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@ActiveProfiles("local")
@DataJpaTest
class BehandlingRepositoryTest {

    @Autowired
    lateinit var behandlingRepository: BehandlingRepository

    @Test
    fun `persist klage works`() {
        val klage = Klagesak(
            foedselsnummer = "12345678910",
            sakstypeId = 1,
            modified = LocalDateTime.now(),
            created = LocalDateTime.now()
        )

        val behandling = Behandling(
            id = UUID.randomUUID(),
            klagesak = klage,
            hjemler = listOf(Hjemmel(
                id = 1,
                paragraf = "8-4"
            )),
            modified = LocalDateTime.now(),
            created = LocalDateTime.now()
        )

        behandlingRepository.save(behandling)

        assertThat(behandlingRepository.findById(behandling.id).get()).isEqualTo(behandling)
    }
}
