package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import io.mockk.mockk
import no.nav.klage.kodeverk.*
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.Registreringshjemmel
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.clients.kaka.KakaApiGateway
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.exceptions.BehandlingAvsluttetException
import no.nav.klage.oppgave.exceptions.BehandlingFinalizedException
import no.nav.klage.oppgave.exceptions.SectionedValidationErrorWithDetailsException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.ApplicationEventPublisher
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
class KlagebehandlingServiceTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Autowired
    lateinit var testEntityManager: TestEntityManager

    @Autowired
    lateinit var mottakRepository: MottakRepository

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @SpykBean
    lateinit var tilgangService: TilgangService

    @MockkBean
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @MockkBean(relaxed = true)
    lateinit var applicationEventPublisher: ApplicationEventPublisher

    @MockkBean
    lateinit var pdlFacade: PdlFacade

    @MockkBean
    lateinit var egenAnsattService: EgenAnsattService

    @MockkBean
    lateinit var saksbehandlerRepository: SaksbehandlerRepository

    @MockkBean
    lateinit var kabalDocumentGateway: KabalDocumentGateway

    @MockkBean
    lateinit var kakaApiGateway: KakaApiGateway

    private val dokumentService: DokumentService = mockk()

    lateinit var klagebehandlingService: KlagebehandlingService

    private val SAKSBEHANDLER_IDENT = "SAKSBEHANDLER_IDENT"
    private val MEDUNDERSKRIVER_IDENT = "MEDUNDERSKRIVER_IDENT"
    private val DOKUMENTENHET_ID = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        klagebehandlingService = KlagebehandlingService(
            klagebehandlingRepository,
            tilgangService,
            applicationEventPublisher,
            dokumentService,
            kabalDocumentGateway,
            kakaApiGateway
        )
    }

    @Nested
    inner class GetKlagebehandlingForUpdate {
        @Test
        fun `getKlagebehandlingForUpdate ok`() {
            val klage = simpleInsert()

            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTil(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit

            assertThat(
                klagebehandlingService.getKlagebehandlingForUpdate(
                    klagebehandlingId = klage.id,
                    ignoreCheckSkrivetilgang = true
                )
            ).isEqualTo(klage)
        }

        @Test
        fun `getKlagebehandlingForUpdate sjekker skrivetilgang, fanger riktig exception`() {
            val klage = simpleInsert()

            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTil(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klage) }.throws(
                BehandlingAvsluttetException("")
            )

            assertThrows<BehandlingAvsluttetException> { klagebehandlingService.getKlagebehandlingForUpdate(klage.id) }
        }
    }

    @Nested
    inner class FerdigstillKlagebehandling {

        @Test
        fun `Forsøk på avslutting av klagebehandling som allerede er avsluttet av saksbehandler skal ikke lykkes`() {
            val klagebehandling = simpleInsert(dokumentEnhetId = true, fullfoert = true)
            every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns SAKSBEHANDLER_IDENT
            every { tilgangService.harInnloggetSaksbehandlerTilgangTil(any()) } returns true
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klagebehandling) } returns Unit

            assertThrows<BehandlingFinalizedException> {
                klagebehandlingService.ferdigstillKlagebehandling(
                    klagebehandling.id,
                    SAKSBEHANDLER_IDENT
                )
            }
        }

//        @Test
//        fun `Forsøk på avslutting av klagebehandling som ikke har mellomlagret dokument skal ikke lykkes`() {
//            val klagebehandling = simpleInsert(false)
//            every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns SAKSBEHANDLER_IDENT
//            every { tilgangService.harInnloggetSaksbehandlerTilgangTil(any()) } returns true
//            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
//            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klagebehandling) } returns Unit
//            every { kakaApiGateway.getValidationErrors(klagebehandling) } returns emptyList()
//
//            assertThrows<SectionedValidationErrorWithDetailsException> {
//                klagebehandlingService.ferdigstillKlagebehandling(
//                    klagebehandling.id,
//                    SAKSBEHANDLER_IDENT
//                )
//            }
//        }

        @Test
        fun `Forsøk på avslutting av klagebehandling som ikke har utfall skal ikke lykkes`() {
            val klagebehandling = simpleInsert(dokumentEnhetId = true, fullfoert = false, utfall = false)
            every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns SAKSBEHANDLER_IDENT
            every { tilgangService.harInnloggetSaksbehandlerTilgangTil(any()) } returns true
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klagebehandling) } returns Unit
            every { kabalDocumentGateway.isHovedDokumentUploaded(DOKUMENTENHET_ID) } returns true
            every { kakaApiGateway.getValidationErrors(klagebehandling) } returns emptyList()

            assertThrows<SectionedValidationErrorWithDetailsException> {
                klagebehandlingService.ferdigstillKlagebehandling(
                    klagebehandling.id,
                    SAKSBEHANDLER_IDENT
                )
            }
        }

        @Test
        fun `Forsøk på avslutting av klagebehandling som ikke har hjemler skal ikke lykkes`() {
            val klagebehandling =
                simpleInsert(dokumentEnhetId = true, fullfoert = false, utfall = true, hjemler = false)
            every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns SAKSBEHANDLER_IDENT
            every { tilgangService.harInnloggetSaksbehandlerTilgangTil(any()) } returns true
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klagebehandling) } returns Unit
            every { kabalDocumentGateway.isHovedDokumentUploaded(DOKUMENTENHET_ID) } returns true
            every { kakaApiGateway.getValidationErrors(klagebehandling) } returns emptyList()

            assertThrows<SectionedValidationErrorWithDetailsException> {
                klagebehandlingService.ferdigstillKlagebehandling(
                    klagebehandling.id,
                    SAKSBEHANDLER_IDENT
                )
            }
        }

        @Test
        fun `Forsøk på avslutting av klagebehandling som er trukket og som ikke har hjemler skal lykkes`() {
            val klagebehandling = simpleInsert(
                dokumentEnhetId = true,
                fullfoert = false,
                utfall = true,
                hjemler = false,
                trukket = true
            )
            every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns SAKSBEHANDLER_IDENT
            every { tilgangService.harInnloggetSaksbehandlerTilgangTil(any()) } returns true
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klagebehandling) } returns Unit
            every { kabalDocumentGateway.isHovedDokumentUploaded(DOKUMENTENHET_ID) } returns true
            every { kakaApiGateway.getValidationErrors(klagebehandling) } returns emptyList()

            val result = klagebehandlingService.ferdigstillKlagebehandling(
                klagebehandling.id,
                MEDUNDERSKRIVER_IDENT
            )
            assertThat(result.currentDelbehandling().avsluttetAvSaksbehandler).isNotNull
        }

        @Test
        fun `Forsøk på avslutting av klagebehandling som er riktig utfylt skal lykkes`() {
            val klagebehandling = simpleInsert(dokumentEnhetId = true)
            every { innloggetSaksbehandlerRepository.getInnloggetIdent() } returns SAKSBEHANDLER_IDENT
            every { tilgangService.harInnloggetSaksbehandlerTilgangTil(any()) } returns true
            every { tilgangService.verifyInnloggetSaksbehandlersTilgangTilYtelse(any()) } returns Unit
            every { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klagebehandling) } returns Unit
            every { kabalDocumentGateway.isHovedDokumentUploaded(DOKUMENTENHET_ID) } returns true
            every { kakaApiGateway.getValidationErrors(klagebehandling) } returns emptyList()

            val result = klagebehandlingService.ferdigstillKlagebehandling(
                klagebehandling.id,
                MEDUNDERSKRIVER_IDENT
            )
            assertThat(result.currentDelbehandling().avsluttetAvSaksbehandler).isNotNull
        }
    }


    private fun simpleInsert(
        dokumentEnhetId: Boolean = false,
        fullfoert: Boolean = false,
        utfall: Boolean = true,
        hjemler: Boolean = true,
        trukket: Boolean = false
    ): Klagebehandling {
        val mottak = Mottak(
            ytelse = Ytelse.OMS_OMP,
            type = Type.KLAGE,
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            kildeReferanse = "1234234",
            sakMottattKaDato = LocalDateTime.now(),
            kildesystem = Fagsystem.K9,
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
            hjemler = if (hjemler) mutableSetOf(
                Hjemmel.FTRL_8_7
            ) else mutableSetOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            mottattKlageinstans = LocalDateTime.now(),
            kildesystem = Fagsystem.K9,
            kildeReferanse = "abc",
            mottakId = mottak.id,
            delbehandlinger = setOf(Delbehandling(
                utfall = when {
                    trukket -> Utfall.TRUKKET
                    utfall -> Utfall.AVVIST
                    else -> null
                },
                hjemler = if (hjemler) mutableSetOf(
                    Registreringshjemmel.ANDRE_TRYGDEAVTALER
                ) else mutableSetOf(),
                dokumentEnhetId = if (dokumentEnhetId) DOKUMENTENHET_ID else null,
                avsluttetAvSaksbehandler = if (fullfoert) LocalDateTime.now() else null,
            )),
            mottattFoersteinstans = LocalDate.now(),
            avsenderEnhetFoersteinstans = "enhet"
        )

        klagebehandlingRepository.save(klage)

        testEntityManager.flush()
        testEntityManager.clear()

        return klage
    }
}
