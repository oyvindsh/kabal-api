package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
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
class KlagebehandlingRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }
    
    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Autowired
    lateinit var mottakRepository: MottakRepository

    /*
    @Test
    fun `persist klage works`() {

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

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottakId = mottak.id
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(klagebehandlingRepository.findById(klage.id).get()).isEqualTo(klage)
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
            kilde = Kilde.OPPGAVE,
            oppgavereferanser = mutableListOf(Oppgavereferanse(oppgaveId = 1001L)),
            status = Status.OPPRETTET.name,
            statusKategori = Status.OPPRETTET.kategoriForStatus().name,
            oversendtKaDato = LocalDate.now()
        )

        mottakRepository.save(mottak)

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottakId = mottak.id,
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
            vedtak = mutableSetOf(Vedtak(
                utfall = Utfall.DELVIS_MEDHOLD,
                created = LocalDateTime.now(),
                modified = LocalDateTime.now()
            ))

        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        val foundklage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundklage.vedtak.first().utfall).isEqualTo(Utfall.DELVIS_MEDHOLD)
        assertThat(foundklage.kvalitetsvurdering?.raadfoertMedLege).isEqualTo(RaadfoertMedLege.MANGLER)
        assertThat(foundklage.hjemler.first().original).isEqualTo("8-5")
    }

    @Test
    fun `persist klage with saksdokumenter works`() {

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

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            saksdokumenter = mutableSetOf(
                Saksdokument(journalpostId = "REF1"),
                Saksdokument(journalpostId = "REF2"),
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottakId = mottak.id
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(klagebehandlingRepository.findById(klage.id).get()).isEqualTo(klage)
    }

    @Test
    fun `remove saksdokument on saved klage works`() {

        testEntityManager.flush()
        testEntityManager.clear()

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

        val klage = Klagebehandling(
            foedselsnummer = "12345678910",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel(
                    original = "8-5"
                )
            ),
            saksdokumenter = mutableSetOf(
                Saksdokument(journalpostId = "REF1"),
                Saksdokument(journalpostId = "REF2"),
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = Kilde.OPPGAVE,
            mottakId = mottak.id
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        val foundklage = klagebehandlingRepository.findById(klage.id).get()
        foundklage.saksdokumenter.removeIf { it.journalpostId == "REF1" }

        testEntityManager.flush()
        testEntityManager.clear()

        val foundModifiedKlage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundModifiedKlage.saksdokumenter).hasSize(1)
        assertThat(foundModifiedKlage.saksdokumenter.first().journalpostId).isEqualTo("REF2")
    }
    */
}
