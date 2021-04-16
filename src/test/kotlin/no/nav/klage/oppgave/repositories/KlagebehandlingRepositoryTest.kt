package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime

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


    @Test
    fun `persist klage works`() {

        val mottak = Mottak(
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            kilde = "OPPGAVE",
            kildeReferanse = "1234234",
            klager = PartId(type = PartIdType.PERSON, value = "23452354"),
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
            kilde = "OPPGAVE",
            mottakId = mottak.id
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(klagebehandlingRepository.findById(klage.id).get()).isEqualTo(klage)
    }

    @Test
    fun `persist klage with saksdokumenter works`() {

        val mottak = Mottak(
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            kilde = "OPPGAVE",
            kildeReferanse = "1234234",
            klager = PartId(type = PartIdType.PERSON, value = "23452354"),
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
                Saksdokument(journalpostId = "REF1", dokumentInfoId = "123"),
                Saksdokument(journalpostId = "REF2", dokumentInfoId = "321"),
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = "OPPGAVE",
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
            kilde = "OPPGAVE",
            kildeReferanse = "1234234",
            klager = PartId(type = PartIdType.PERSON, value = "23452354"),
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
                Saksdokument(journalpostId = "REF1", dokumentInfoId = "123"),
                Saksdokument(journalpostId = "REF2", dokumentInfoId = "321"),
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDate.now(),
            kilde = "OPPGAVE",
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

}
