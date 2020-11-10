package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.Tilganger
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.pdl.*
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.TYPE_ANKE
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveServiceTest {

    @Test
    fun `type is klage`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_KLAGE)
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk(relaxed = true)).oppgaver.first().type).isEqualTo(TYPE_KLAGE)
    }

    @Test
    fun `type is anke`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_ANKE)
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk(relaxed = true)).oppgaver.first().type).isEqualTo(
            TYPE_ANKE
        )
    }

    @Test
    fun `unknown type`() {
        val oppgaveService = oppgaveServiceWithType("somethingelse")
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk(relaxed = true)).oppgaver.first().type).isEqualTo("ukjent")
    }

    @Test
    fun `hjemmel is set correctly`() {
        val hjemmel = "8-1"
        val oppgaveService = oppgaveServiceWithHjemmel(hjemmel)
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk(relaxed = true)).oppgaver.first().hjemmel).isEqualTo(hjemmel)
    }

    @Test
    fun `missing hjemmel does not fail`() {
        val oppgaveService = oppgaveServiceWithType("something")
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk(relaxed = true)).oppgaver.first().hjemmel).isEqualTo("mangler")
    }

    @Test
    fun `fnr is mapped correctly`() {
        val fnr = "12345678910"
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOneSearchPage(any()) } returns getOppgaveResponseWithIdenter(fnr)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(listOf(fnr)) } returns getHentPersonResponse()

        val saksbehandlerRepositoryMock = mockk<SaksbehandlerRepository>()
        every { saksbehandlerRepositoryMock.getTilgangerForSaksbehandler(any()) } returns Tilganger(
            arrayOf(
                mockk(
                    relaxed = true
                )
            )
        )

        val oppgaveService = OppgaveService(
            oppgaveClient,
            pdlClientMock,
            mockk(relaxed = true),
            saksbehandlerRepositoryMock
        )

        assertThat(oppgaveService.searchTildelteOppgaver("", mockk(relaxed = true)).oppgaver.first().bruker.fnr).isEqualTo(fnr)
    }

    private fun getHentPersonResponse(): HentPersonResponse {
        return HentPersonResponse(
            data = HentPersonBolk(
                listOf(
                    Person(
                        navn = listOf(
                            Navn(
                                fornavn = "Test",
                                etternavn = "Person"
                            )
                        ),
                        folkeregisteridentifikator = listOf(
                            Folkeregisteridentifikator(
                                identifikasjonsnummer = "12345678910",
                                type = "FNR",
                                status = ""
                            )
                        )
                    )
                )
            )
        )
    }

    private fun oppgaveServiceWithHjemmel(hjemmel: String): OppgaveService {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOneSearchPage(any()) } returns getOppgaveResponseWithHjemmel(hjemmel)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        val saksbehandlerRepositoryMock = mockk<SaksbehandlerRepository>()
        every { saksbehandlerRepositoryMock.getTilgangerForSaksbehandler(any()) } returns Tilganger(
            arrayOf(
                mockk(
                    relaxed = true
                )
            )
        )

        val oppgaveService = OppgaveService(
            oppgaveClientMock,
            pdlClientMock,
            mockk(relaxed = true),
            saksbehandlerRepositoryMock
        )
        return oppgaveService
    }

    private fun oppgaveServiceWithType(type: String): OppgaveService {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOneSearchPage(any()) } returns getOppgaveResponseWithType(type)

        val saksbehandlerRepositoryMock = mockk<SaksbehandlerRepository>()
        every { saksbehandlerRepositoryMock.getTilgangerForSaksbehandler(any()) } returns Tilganger(
            arrayOf(
                mockk(
                    relaxed = true
                )
            )
        )

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        return OppgaveService(
            oppgaveClientMock,
            pdlClientMock,
            mockk(relaxed = true),
            saksbehandlerRepositoryMock
        )
    }

    private fun getOppgaveResponseWithType(type: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                behandlingstype = type,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK",
                versjon = 0
            )
        )
    )

    private fun getOppgaveResponseWithHjemmel(hjemmel: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK",
                metadata = mapOf(HJEMMEL to hjemmel),
                versjon = 0
            )
        )
    )

    private fun getOppgaveResponseWithIdenter(fnr: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK",
                identer = listOf(
                    Ident(
                        ident = "321321",
                        gruppe = Gruppe.AKTOERID
                    ),
                    Ident(
                        ident = fnr,
                        gruppe = Gruppe.FOLKEREGISTERIDENT
                    )
                ),
                versjon = 0
            )
        )
    )

}