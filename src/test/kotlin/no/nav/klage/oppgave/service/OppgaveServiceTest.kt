package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.pdl.*
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.TYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.klage.oppgave.repositories.OppgaveRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveServiceTest {

    @Test
    fun `type is klage`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_KLAGE)
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk()).oppgaver.first().type).isEqualTo(TYPE_KLAGE)
    }

    @Test
    fun `type is feilutbetaling`() {
        val oppgaveService = oppgaveServiceWithType(BEHANDLINGSTYPE_FEILUTBETALING)
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk()).oppgaver.first().type).isEqualTo(
            TYPE_FEILUTBETALING
        )
    }

    @Test
    fun `unknown type`() {
        val oppgaveService = oppgaveServiceWithType("somethingelse")
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk()).oppgaver.first().type).isEqualTo("mangler")
    }

    @Test
    fun `hjemmel is set correctly`() {
        val hjemmel = "8-1"
        val oppgaveService = oppgaveServiceWithHjemmel(hjemmel)
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk()).oppgaver.first().hjemmel).isEqualTo(hjemmel)
    }

    @Test
    fun `missing hjemmel does not fail`() {
        val oppgaveService = oppgaveServiceWithType("something")
        assertThat(oppgaveService.searchTildelteOppgaver("", mockk()).oppgaver.first().hjemmel).isEqualTo("mangler")
    }

    @Test
    fun `fnr is mapped correctly`() {
        val fnr = "12345678910"
        val oppgaveRepository = mockk<OppgaveRepository>()
        every { oppgaveRepository.searchOppgaver(any(), any()) } returns getOppgaveResponseWithIdenter(fnr)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(listOf(fnr)) } returns getHentPersonResponse()

        val oppgaveService = OppgaveService(
            oppgaveRepository,
            pdlClientMock,
            mockk(relaxed = true)
        )

        assertThat(oppgaveService.searchTildelteOppgaver("", mockk()).oppgaver.first().bruker.fnr).isEqualTo(fnr)
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
        val oppgaveRepositoryMock = mockk<OppgaveRepository>()
        every { oppgaveRepositoryMock.searchOppgaver(any(), any()) } returns getOppgaveResponseWithHjemmel(hjemmel)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        val oppgaveService = OppgaveService(
            oppgaveRepositoryMock,
            pdlClientMock,
            mockk(relaxed = true)
        )
        return oppgaveService
    }

    private fun oppgaveServiceWithType(type: String): OppgaveService {
        val oppgaveRepositoryMock = mockk<OppgaveRepository>()
        every { oppgaveRepositoryMock.searchOppgaver(any(), any()) } returns getOppgaveResponseWithType(type)

        val pdlClientMock = mockk<PdlClient>()
        every { pdlClientMock.getPersonInfo(any()) } returns getHentPersonResponse()

        return OppgaveService(
            oppgaveRepositoryMock,
            pdlClientMock,
            mockk(relaxed = true)
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