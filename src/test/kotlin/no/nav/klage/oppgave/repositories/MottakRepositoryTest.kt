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
            sakstype = Sakstype.KLAGE,
            klagerPart = KlagerPart(partId = PartId(type = PartIdType.PERSON, value = "123454")),
            sakReferanse = "12345",
            kildeReferanse = "54321",
            dvhReferanse = "5342523",
            hjemmelListe = "8-4",
            avsenderSaksbehandlerident = "Z123456",
            avsenderEnhet = "1234",
            mottakDokument = mutableSetOf(MottakDokument(
                type = MottakDokumentType.OVERSENDELSESBREV,
                journalpostId = "245245"
            )),
            brevmottakere = mutableSetOf(PartId(
                type = PartIdType.PERSON,
                value = "746574"
            )),
            oversendtKaDato = LocalDate.now(),
            kilde = "Kilde"
        )

        mottakRepository.save(mottak)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(mottakRepository.findById(mottak.id).get()).isEqualTo(mottak)
    }

}
