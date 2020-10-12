package no.nav.klage.oppgave.service

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.TYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
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

        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo(TYPE_KLAGE)
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

        assertThat(oppgaveService.getOppgaver().first().type).isEqualTo(TYPE_FEILUTBETALING)
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

    @Test
    fun `hjemmel is set correctly`() {
        val oppgaveClient = mockk<OppgaveClient>()
        val hjemmel = "8-1"
        every { oppgaveClient.getOppgaver() } returns getOppgaveResponseWithHjemmel(hjemmel)

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClient,
            mockk()
        )

        assertThat(oppgaveService.getOppgaver().first().hjemmel.first()).isEqualTo(hjemmel)
    }

    @Test
    fun `missing hjemmel does not fail`() {
        val oppgaveClient = mockk<OppgaveClient>()
        every { oppgaveClient.getOppgaver() } returns getOppgaveResponseWithType("something")

        val oppgaveService = OppgaveService(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            oppgaveClient,
            mockk()
        )

        assertThat(oppgaveService.getOppgaver().first().hjemmel.first()).isEqualTo("mangler")
    }

    private fun getOppgaveResponseWithType(type: String) = OppgaveResponse(
        antallTreffTotalt = 1,
        oppgaver = listOf(
            Oppgave(
                id = 1,
                behandlingstype = type,
                fristFerdigstillelse = LocalDate.now(),
                tema = "SYK"
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
                metadata = mapOf(HJEMMEL to hjemmel)
            )
        )
    )

}