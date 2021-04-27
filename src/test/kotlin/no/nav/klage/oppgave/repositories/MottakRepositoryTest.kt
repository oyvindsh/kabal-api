package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.api.view.Fagsystem
import no.nav.klage.oppgave.api.view.Lov
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
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

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MottakRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var mottakRepository: MottakRepository

    @Test
    fun `persist mottak works`() {
        val mottak = Mottak(
            tema = Tema.SYK,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "123454")),
            sakFagsakId = "12345",
            sakFagsystem = Fagsystem.AO01,
            kildeReferanse = "54321",
            dvhReferanse = "5342523",
            hjemmelListe = mutableSetOf(MottakHjemmel(lov = Lov.FOLKETRYGDLOVEN, kapittel = 8, paragraf = 4)),
            avsenderSaksbehandlerident = "Z123456",
            avsenderEnhet = "1234",
            mottakDokument = mutableSetOf(
                MottakDokument(
                    type = MottakDokumentType.OVERSENDELSESBREV,
                    journalpostId = "245245"
                )
            ),
            oversendtKaDato = LocalDate.now(),
            kilde = "Kilde"
        )

        mottakRepository.save(mottak)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(mottakRepository.findById(mottak.id).get()).isEqualTo(mottak)
    }

}
