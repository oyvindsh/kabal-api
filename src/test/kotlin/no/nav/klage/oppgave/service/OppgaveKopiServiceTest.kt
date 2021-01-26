package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.oppgavekopi.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.PersistenceException

@ActiveProfiles("local")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OppgaveKopiService::class, KlagebehandlingService::class, HjemmelService::class)
class OppgaveKopiServiceTest {

    @Autowired
    lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var oppgaveKopiService: OppgaveKopiService

    @Test
    fun oppgaveKopiWithOnlyMandatoryValuesShouldBeStoredProperly() {

        val now = LocalDateTime.now()
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
            opprettetTidspunkt = now,
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgave = oppgaveKopiService.getOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.opprettetTidspunkt).isEqualTo(now)
    }


    @Test
    fun oppgaveKopiWithIdentShouldBeStoredProperly() {
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
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgave = oppgaveKopiService.getOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.ident).isNotNull
        assertThat(hentetOppgave.ident?.verdi).isEqualTo("12345")
    }

    @Test
    fun oppgaveKopiWithMetadataShouldBeStoredProperly() {
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
            opprettetTidspunkt = LocalDateTime.now(),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgave = oppgaveKopiService.getOppgaveKopi(oppgaveKopi.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.metadata).isNotNull
        assertThat(hentetOppgave.metadata.size).isEqualTo(1)
        assertThat(hentetOppgave.metadataAsMap()[MetadataNoekkel.HJEMMEL]).isEqualTo("8-25")

//        val versjonMetadataCount = jdbcTemplate.queryForObject(
//            "SELECT count(*) FROM oppgave.versjonmetadata",
//            emptyArray(),
//            Integer::class.java
//        )
//        assertThat(versjonMetadataCount).isEqualTo(1)
    }

    @Test
    fun `two versions of OppgaveKopi should be stored properly`() {
        val oppgaveKopi1 = OppgaveKopi(
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
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )
        val oppgaveKopi2 = OppgaveKopi(
            id = 1001L,
            versjon = 2,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi1)

        entityManager.flush()
        entityManager.clear()

        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi2)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgave = oppgaveKopiService.getOppgaveKopi(oppgaveKopi1.id)
        assertThat(hentetOppgave).isNotNull
        assertThat(hentetOppgave.metadata.size).isEqualTo(1)
    }

    @Test
    fun `storing the same version twice throws ConstraintViolationException`() {
        val oppgaveKopi1 = OppgaveKopi(
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
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )

        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi1)

        entityManager.flush()
        entityManager.clear()

        assertThatExceptionOfType(PersistenceException::class.java)
            .isThrownBy {
                oppgaveKopiService.saveOppgaveKopi(oppgaveKopi1)

                entityManager.flush()
                entityManager.clear()
            }
            .withCauseInstanceOf(ConstraintViolationException::class.java)
    }

    @Test
    fun oppgaveversjonShouldBeStoredProperly() {
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
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgaveversjon = oppgaveKopiService.getOppgaveKopiVersjon(oppgaveKopi.id, oppgaveKopi.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
        assertThat(hentetOppgaveversjon.opprettetTidspunkt).isEqualTo(oppgaveKopi.opprettetTidspunkt)

        val hentetOppgaveSisteVersjon =
            oppgaveKopiService.getOppgaveKopiSisteVersjon(oppgaveKopi.id)
        assertThat(hentetOppgaveSisteVersjon).isNotNull
        assertThat(hentetOppgaveSisteVersjon.opprettetTidspunkt).isEqualTo(oppgaveKopi.opprettetTidspunkt)
    }

    @Test
    fun storingTheSameOppgaveversjonTwiceShouldNotCauseError() {
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
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgaveversjon = oppgaveKopiService.getOppgaveKopiVersjon(oppgaveKopi.id, oppgaveKopi.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
    }

    @Test
    fun storingTwoOppgaveversjonsShouldWorkProperly() {
        val oppgaveKopi1 = OppgaveKopi(
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
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )
        val oppgaveKopi2 = OppgaveKopi(
            id = 1001L,
            versjon = 2,
            tema = "tema",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "KLAGE",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            ident = Ident(null, IdentType.AKTOERID, "12345", "12345678910", null),
            metadata = setOf(Metadata(null, MetadataNoekkel.HJEMMEL, "8-25")),
            behandlingstype = "ae0058"
        )
        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi2)

        entityManager.flush()
        entityManager.clear()

        oppgaveKopiService.saveOppgaveKopi(oppgaveKopi1)

        entityManager.flush()
        entityManager.clear()

        val hentetOppgaveversjon = oppgaveKopiService.getOppgaveKopiVersjon(oppgaveKopi1.id, oppgaveKopi1.versjon)
        assertThat(hentetOppgaveversjon).isNotNull
        assertThat(hentetOppgaveversjon.opprettetTidspunkt).isEqualTo(oppgaveKopi1.opprettetTidspunkt)

        val hentetOppgaveSisteVersjon =
            oppgaveKopiService.getOppgaveKopiSisteVersjon(oppgaveKopi2.id)
        assertThat(hentetOppgaveSisteVersjon).isNotNull
        assertThat(hentetOppgaveSisteVersjon.opprettetTidspunkt).isEqualTo(oppgaveKopi2.opprettetTidspunkt)

    }

}
