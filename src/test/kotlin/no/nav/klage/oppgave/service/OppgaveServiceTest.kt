package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.Oppgave
import no.nav.klage.oppgave.domain.OppgaveResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class OppgaveServiceTest {

    @Test
    fun `type is klage`() {
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOppgaver() } returns getOppgaveResponseWithType(BEHANDLINGSTYPE_KLAGE)

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClient,
            mockk()
        )

        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo("klage")
    }

    @Test
    fun `type is feilutbetaling`() {
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOppgaver() } returns getOppgaveResponseWithType(BEHANDLINGSTYPE_FEILUTBETALING)

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClient,
            mockk()
        )

        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo("feilutbetaling")
    }

    @Test
    fun `unknown type`() {
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOppgaver() } returns getOppgaveResponseWithType("somethingelse")

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClient,
            mockk()
        )

        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo("mangler")
    }

    private fun getOppgaveResponseWithType(type: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(Oppgave(
            id = 1,
            behandlingstype = type,
            fristFerdigstillelse = LocalDate.now(),
            tema = "SYK"
        ))
    )

}