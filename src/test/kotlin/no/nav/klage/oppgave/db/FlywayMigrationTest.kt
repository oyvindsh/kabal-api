package no.nav.klage.oppgave.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.sql.ResultSet


@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FlywayMigrationTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate

    data class Utfall(val id: Long, val navn: String)

    @Test
    fun flyway_should_run() {
        val statuser: List<Utfall> = jdbcTemplate.query(
            "SELECT * FROM kodeverk.utfall"
        ) { rs: ResultSet, _: Int ->
            Utfall(
                rs.getLong("id"),
                rs.getString("navn")
            )
        }

        assertThat(statuser).hasSize(8)
    }

}