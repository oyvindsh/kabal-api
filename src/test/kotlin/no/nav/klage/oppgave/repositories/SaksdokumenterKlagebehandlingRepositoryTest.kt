package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.domain.kodeverk.Kilde
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
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
class SaksdokumenterKlagebehandlingRepositoryTest {

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Test
    fun `persist klage with saksdokumenter works`() {

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
            saksdokumenter = mutableListOf(
                Saksdokument(referanse = "REF1"),
                Saksdokument(referanse = "REF2"),
            ),
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
    fun `remove saksdokument on saved klage works`() {

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
            oppgavereferanser = mutableListOf(),
            saksdokumenter = mutableListOf(
                Saksdokument(referanse = "REF1"),
                Saksdokument(referanse = "REF2"),
            ),
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
        foundklage.saksdokumenter.removeIf { it.referanse == "REF1" }

        testEntityManager.flush()
        testEntityManager.clear()

        val foundModifiedKlage = klagebehandlingRepository.findById(klage.id).get()
        assertThat(foundModifiedKlage.saksdokumenter).hasSize(1)
        assertThat(foundModifiedKlage.saksdokumenter.first().referanse).isEqualTo("REF2")
    }
}
