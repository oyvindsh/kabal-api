package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Oppgavereferanse
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.domain.oppgavekopi.Prioritet
import no.nav.klage.oppgave.domain.oppgavekopi.Status
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime

@ActiveProfiles("local")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class KlagebehandlingRepositoryTest {

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Autowired
    lateinit var oppgaveKopiRepository: OppgaveKopiRepository

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

    @Test
    fun `persist klage with link to oppgave works`() {
        val oppgaveKopi = OppgaveKopi(
            id = 1001L,
            versjon = 1,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now()
        )
        oppgaveKopiRepository.save(oppgaveKopi)

        testEntityManager.flush()
        testEntityManager.clear()

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            sakstype = Sakstype(1, "", ""),
            hjemler = listOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            oppgavereferanser = listOf(Oppgavereferanse(oppgaveId = 1001L)),
            modified = LocalDateTime.now(),
            created = LocalDateTime.now(),
            frist = LocalDate.now()
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        val foundklage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundklage.oppgavereferanser[0].oppgaveId).isEqualTo(1001L)
    }
}
