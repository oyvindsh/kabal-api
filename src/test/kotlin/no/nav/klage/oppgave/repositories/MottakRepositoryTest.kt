package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.Oppgavereferanse
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
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
class MottakRepositoryTest {

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var mottakRepository: MottakRepository

    @Autowired
    lateinit var oppgaveKopiRepository: OppgaveKopiRepository

    @Test
    fun `persist mottak works`() {
        val mottak = Mottak(
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            kilde = Kilde.OPPGAVE,
            oppgavereferanser = mutableListOf(),
            status = Status.OPPRETTET.name,
            statusKategori = Status.OPPRETTET.kategoriForStatus().name,
            oversendtKaDato = LocalDate.now()
        )

        mottakRepository.save(mottak)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(mottakRepository.findById(mottak.id).get()).isEqualTo(mottak)
    }

    @Test
    fun `persist mottak with link to oppgave works`() {
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

        val mottak = Mottak(
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            kilde = Kilde.OPPGAVE,
            oppgavereferanser = mutableListOf(Oppgavereferanse(oppgaveId = 1001L)),
            status = Status.OPPRETTET.name,
            statusKategori = Status.OPPRETTET.kategoriForStatus().name,
            oversendtKaDato = LocalDate.now()
        )

        mottakRepository.save(mottak)

        testEntityManager.flush()
        testEntityManager.clear()

        val foundMottak = mottakRepository.findById(mottak.id).get()
        assertThat(foundMottak.oppgavereferanser[0].oppgaveId).isEqualTo(1001L)
    }
}
