package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.api.view.HJEMMEL
import no.nav.klage.oppgave.api.view.TYPE_ANKE
import no.nav.klage.oppgave.api.view.TYPE_KLAGE
import no.nav.klage.oppgave.clients.gosys.OppgaveClient
import no.nav.klage.oppgave.clients.pdl.PdlClient
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.pdl.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveServiceTest {

    @Test
    fun `type is klage`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_KLAGE)
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().type).isEqualTo(TYPE_KLAGE)
    }

    @Test
    fun `type is anke`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_ANKE)
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().type).isEqualTo(
            TYPE_ANKE
        )
    }

    @Test
    fun `unknown type`() {
        val oppgaveService = oppgaveServiceWithType("somethingelse")
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().type).isEqualTo("ukjent")
    }

    @Test
    fun `hjemmel is set correctly`() {
        val hjemmel = "8-1"
        val oppgaveService = oppgaveServiceWithHjemmel(hjemmel)
        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().hjemmel).isEqualTo(hjemmel)
    }

    @Test
    fun `missing hjemmel does not fail`() {
        val oppgaveService = oppgaveServiceWithType("something")

        assertThat(oppgaveService.searchOppgaver(mockk(relaxed = true)).oppgaver.first().hjemmel).isEqualTo("mangler")
    }

    @Test
    fun `fnr is mapped correctly`() {
        val fnr = "12345678910"
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOneSearchPage(any()) } returns getOppgaveResponseWithIdenter(fnr)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(listOf(fnr)) } returns getHentPersonResponse()

        val oppgaveService = OppgaveService(
            oppgaveClient,
            OppgaveMapper(pdlClientMock)
        )

        val oppgaverSearchCriteriaMock = mockk<OppgaverSearchCriteria>(relaxed = true)
        every { oppgaverSearchCriteriaMock.isProjectionUtvidet() } returns true

        assertThat(
            oppgaveService.searchOppgaver(oppgaverSearchCriteriaMock).oppgaver.first().person?.fnr
        ).isEqualTo(fnr)
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

        val oppgaveService = OppgaveService(
            oppgaveClientMock,
            OppgaveMapper(pdlClientMock)
        )
        return oppgaveService
    }

    private fun oppgaveServiceWithType(type: String): OppgaveService {
        val oppgaveClientMock = mockk<OppgaveClient>()
        every { oppgaveClientMock.getOneSearchPage(any()) } returns getOppgaveResponseWithType(type)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        return OppgaveService(
            oppgaveClientMock,
            OppgaveMapper(pdlClientMock)
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