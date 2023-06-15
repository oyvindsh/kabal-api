package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.exceptions.BehandlingAvsluttetException
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class TilgangServiceTest {

    private val pdlFacade: PdlFacade = mockk()

    private val egenAnsattService: EgenAnsattService = mockk()

    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService = mockk()

    private val saksbehandlerRepository: SaksbehandlerRepository = mockk()

    private val saksbehandlerService: SaksbehandlerService = mockk()

    private val tilgangService =
        TilgangService(
            pdlFacade,
            egenAnsattService,
            innloggetSaksbehandlerService,
            saksbehandlerRepository,
            saksbehandlerService
        )

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil ved avsluttet`() {
        val klage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
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
            fagsystem = Fagsystem.K9,
            fagsakId = "123",
            kildeReferanse = "abc",
            mottakId = UUID.randomUUID(),
            avsenderEnhetFoersteinstans = "4100",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(
                Delbehandling(
                    avsluttet = LocalDateTime.now(),
                )
            ),
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            kakaKvalitetsvurderingVersion = 2,
        )

        assertThrows<BehandlingAvsluttetException> {
            tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(
                klage
            )
        }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil ved avsluttet av saksbehandler`() {
        val klage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
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
            fagsystem = Fagsystem.K9,
            fagsakId = "123",
            kildeReferanse = "abc",
            mottakId = UUID.randomUUID(),
            avsenderEnhetFoersteinstans = "4100",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(
                Delbehandling(
                    avsluttetAvSaksbehandler = LocalDateTime.now(),
                )
            ),
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            kakaKvalitetsvurderingVersion = 2,
        )

        assertThrows<BehandlingAvsluttetException> {
            tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(
                klage
            )
        }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil ved annen tildelt saksbehandler`() {
        val klage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
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
            fagsystem = Fagsystem.K9,
            fagsakId = "123",
            kildeReferanse = "abc",
            mottakId = UUID.randomUUID(),
            tildeling = Tildeling(saksbehandlerident = "Z123456", enhet = "", tidspunkt = LocalDateTime.now()),
            avsenderEnhetFoersteinstans = "4100",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            kakaKvalitetsvurderingVersion = 2,
        )

        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z654321")

        assertThrows<MissingTilgangException> { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klage) }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir feil når ingen har tildelt`() {
        val klage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
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
            fagsystem = Fagsystem.K9,
            fagsakId = "123",
            kildeReferanse = "abc",
            mottakId = UUID.randomUUID(),
            avsenderEnhetFoersteinstans = "4100",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            kakaKvalitetsvurderingVersion = 2,
        )

        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z654321")

        assertThrows<MissingTilgangException> { tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klage) }
    }

    @Test
    fun `verifySaksbehandlersSkrivetilgang gir ok ved samme ident`() {
        val klage = Klagebehandling(
            klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
            sakenGjelder = SakenGjelder(
                partId = PartId(type = PartIdType.PERSON, value = "23452354"),
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
            fagsystem = Fagsystem.K9,
            fagsakId = "123",
            kildeReferanse = "abc",
            mottakId = UUID.randomUUID(),
            tildeling = Tildeling(saksbehandlerident = "Z123456", enhet = "", tidspunkt = LocalDateTime.now()),
            avsenderEnhetFoersteinstans = "4100",
            mottattVedtaksinstans = LocalDate.now(),
            delbehandlinger = setOf(Delbehandling()),
            kakaKvalitetsvurderingId = UUID.randomUUID(),
            kakaKvalitetsvurderingVersion = 2,
        )

        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")

        assertThat(tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(klage)).isEqualTo(Unit)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på fortrolig`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleFortrolig() }.returns(false)
        every { innloggetSaksbehandlerService.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på strengt fortrolig`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.STRENGT_FORTROLIG,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på egen ansatt`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = null,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir true på egen ansatt når saksbehandler har egenAnsatt rettigheter`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = null,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleEgenAnsatt() }.returns(true)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(true)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir true på fortrolig når saksbehandler har fortrolig rettigheter`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerService.kanBehandleFortrolig() }.returns(true)
        every { innloggetSaksbehandlerService.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(true)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir false på fortrolig når saksbehandler har strengt fortrolig rettigheter`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerService.kanBehandleFortrolig() }.returns(false)
        every { innloggetSaksbehandlerService.kanBehandleStrengtFortrolig() }.returns(true)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(false)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(false)
    }

    @Test
    fun `harSaksbehandlerTilgangTil gir true på fortrolig kombinert med egen ansatt når saksbehandler har fortrolig rettigheter men ikke egen ansatt`() {
        every { pdlFacade.getPersonInfo(any()) }.returns(
            Person(
                foedselsnr = "",
                fornavn = "",
                mellomnavn = "",
                etternavn = "",
                sammensattNavn = "",
                beskyttelsesbehov = Beskyttelsesbehov.FORTROLIG,
                kjoenn = "",
                sivilstand = null,
                vergemaalEllerFremtidsfullmakt = false,
            )
        )

        every { innloggetSaksbehandlerService.kanBehandleEgenAnsatt() }.returns(false)
        every { innloggetSaksbehandlerService.kanBehandleFortrolig() }.returns(true)
        every { innloggetSaksbehandlerService.kanBehandleStrengtFortrolig() }.returns(false)
        every { innloggetSaksbehandlerService.getInnloggetIdent() }.returns("Z123456")
        every { egenAnsattService.erEgenAnsatt(any()) }.returns(true)
        assertThat(tilgangService.harInnloggetSaksbehandlerTilgangTil("")).isEqualTo(true)
    }
}
