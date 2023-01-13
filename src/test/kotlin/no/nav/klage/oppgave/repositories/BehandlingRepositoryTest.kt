package no.nav.klage.oppgave.repositories

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
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
import java.util.*

@ActiveProfiles("local")
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BehandlingRepositoryTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var behandlingRepository: BehandlingRepository

    @Autowired
    lateinit var mottakRepository: MottakRepository

    private val ENHET_1 = "ENHET_1"
    private val ENHET_2 = "ENHET_2"

    @Test
    fun `store Klagebehandling works`() {

        val mottak = Mottak(
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            kildeReferanse = "1234234",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            forrigeBehandlendeEnhet = "0101",
            brukersHenvendelseMottattNavDato = LocalDate.now()
        )

        mottakRepository.save(mottak)

        val klage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTRL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            kildeReferanse = "abc",
            mottakId = mottak.id,
            avsenderEnhetFoersteinstans = "0101",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingVersion = 2,
            kakaKvalitetsvurderingId = UUID.randomUUID()
        )

        behandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        assertThat(behandlingRepository.findById(klage.id).get()).isEqualTo(klage)
    }

    @Test
    fun `enhet based query works`() {

        val mottak1 = Mottak(
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            kildeReferanse = "1234234",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            forrigeBehandlendeEnhet = "0101",
            brukersHenvendelseMottattNavDato = LocalDate.now()
        )

        mottakRepository.save(mottak1)

        val klageTildeltEnhet1 = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTRL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            kildeReferanse = "abc",
            mottakId = mottak1.id,
            avsenderEnhetFoersteinstans = "0101",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingVersion = 2,
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            tildeling = Tildeling(
                saksbehandlerident = "1", enhet = ENHET_1, tidspunkt = LocalDateTime.now()

            )
        )

        val mottak2 = Mottak(
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            kildeReferanse = "1234234",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            forrigeBehandlendeEnhet = "0101",
            brukersHenvendelseMottattNavDato = LocalDate.now()
        )

        mottakRepository.save(mottak2)

        val klageTildeltEnhet2 = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTRL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            kildeReferanse = "abc",
            mottakId = mottak2.id,
            avsenderEnhetFoersteinstans = "0101",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingVersion = 2,
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            tildeling = Tildeling(
                saksbehandlerident = "1", enhet = ENHET_2, tidspunkt = LocalDateTime.now()

            )
        )

        val mottak3 = Mottak(
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            kildeReferanse = "1234234",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            forrigeBehandlendeEnhet = "0101",
            brukersHenvendelseMottattNavDato = LocalDate.now()
        )

        mottakRepository.save(mottak3)

        val klageUtenTildeling = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTRL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            kildeReferanse = "abc",
            mottakId = mottak3.id,
            avsenderEnhetFoersteinstans = "0101",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingVersion = 2,
            kakaKvalitetsvurderingId = UUID.randomUUID(),
        )

        val mottak4 = Mottak(
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            kildeReferanse = "1234234",
            sakMottattKaDato = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            forrigeBehandlendeEnhet = "0101",
            brukersHenvendelseMottattNavDato = LocalDate.now()
        )

        mottakRepository.save(mottak4)

        val fullfoertKlage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                skalMottaKopi = false
            ),
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            frist = LocalDate.now(),
            hjemler = mutableSetOf(
                Hjemmel.FTRL_8_7
            ),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            sakFagsystem = Fagsystem.K9,
            kildeReferanse = "abc",
            mottakId = mottak4.id,
            avsenderEnhetFoersteinstans = "0101",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling(avsluttetAvSaksbehandler = LocalDateTime.now())),
            kakaKvalitetsvurderingVersion = 2,
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            tildeling = Tildeling(
                saksbehandlerident = "1", enhet = ENHET_1, tidspunkt = LocalDateTime.now()
            ),

            )

        behandlingRepository.saveAll(listOf(klageTildeltEnhet1, klageTildeltEnhet2, klageUtenTildeling, fullfoertKlage))

        testEntityManager.flush()
        testEntityManager.clear()
        val result =
            behandlingRepository.findByTildelingEnhetAndDelbehandlingerAvsluttetAvSaksbehandlerIsNull(enhet = ENHET_1)

        assertThat(result).isEqualTo(listOf(klageTildeltEnhet1))
    }
}
