package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.*
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
        val mottak = Mottak(
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            kilde = Kilde.OPPGAVE
        )

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableListOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            oppgavereferanser = mutableListOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottak = mottak
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

        val mottak = Mottak(
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            kilde = Kilde.OPPGAVE
        )

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableListOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            oppgavereferanser = mutableListOf(Oppgavereferanse(oppgaveId = 1001L)),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottak = mottak
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        val foundklage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundklage.oppgavereferanser[0].oppgaveId).isEqualTo(1001L)
    }

    @Test
    fun `persist klage with everything works`() {
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
            kilde = Kilde.OPPGAVE
        )

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableListOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            oppgavereferanser = mutableListOf(Oppgavereferanse(oppgaveId = 1001L)),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottak = mottak,
            kvalitetsvurdering = Kvalitetsvurdering(
                grunn = Grunn.ANDRE_SAKSBEHANDLINGSFEIL,
                eoes = Eoes.IKKE_OPPDAGET,
                raadfoertMedLege = RaadfoertMedLege.MANGLER,
                internVurdering = "Min interne vurdering",
                sendTilbakemelding = true,
                tilbakemelding = "Skjerpings!",
                mottakerSaksbehandlerident = null,
                mottakerEnhet = null,
                created = LocalDateTime.now(),
                modified = LocalDateTime.now()
            ),
            vedtak = Vedtak(
                utfall = Utfall.DELVIS_MEDHOLD,
                created = LocalDateTime.now(),
                modified = LocalDateTime.now()
            )

        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        val foundklage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundklage.oppgavereferanser[0].oppgaveId).isEqualTo(1001L)
        assertThat(foundklage.vedtak?.utfall).isEqualTo(Utfall.DELVIS_MEDHOLD)
        assertThat(foundklage.kvalitetsvurdering?.raadfoertMedLege).isEqualTo(RaadfoertMedLege.MANGLER)
        assertThat(foundklage.hjemler[0].original).isEqualTo("8-5")
    }
}
