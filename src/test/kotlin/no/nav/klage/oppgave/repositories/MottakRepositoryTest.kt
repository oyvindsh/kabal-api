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

/*    @Test
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
    }*/

}
