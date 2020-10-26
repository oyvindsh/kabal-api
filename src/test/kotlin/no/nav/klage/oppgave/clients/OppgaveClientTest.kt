package no.nav.klage.oppgave.clients

import brave.Tracer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDate
import java.time.Month

@ExtendWith(MockKExtension::class)
internal class OppgaveClientTest {

    @MockK
    lateinit var stsClientMock: StsClient

    @MockK
    lateinit var tracerMock: Tracer

    @BeforeEach
    fun before() {
        every { stsClientMock.oidcToken() } returns "abc"
        every { tracerMock.currentSpan().context().traceIdString() } returns "def"
    }

    @Test
    fun `frist is parsed correctly`() {
        val oppgaveResponse = getOppgaver(oppgaveResponseWithFrist())
        assertThat(oppgaveResponse.oppgaver.first().fristFerdigstillelse).isEqualTo(
            LocalDate.of(2020, Month.SEPTEMBER, 3)
        )
    }

    @Test
    fun `null frist is OK`() {
        val oppgaveResponse = getOppgaver(oppgaveResponseWithoutFrist())
        assertThat(oppgaveResponse.oppgaver.first().fristFerdigstillelse).isNull()
    }

    @Test
    fun `missing frist field is OK`() {
        val oppgaveResponse = getOppgaver(oppgaveResponseWithoutFristField())
        assertThat(oppgaveResponse.oppgaver.first().fristFerdigstillelse).isNull()
    }

    fun getOppgaver(jsonResponse: String): OppgaveResponse {
        val oppgaveClient = OppgaveClient(
            createShortCircuitWebClient(jsonResponse),
            stsClientMock,
            tracerMock,
            "appName"
        )

        return oppgaveClient.getOnePage(0)
    }

    @Language("json")
    fun oppgaveResponseWithFrist() = """
        {
          "antallTreffTotalt": 1,
          "oppgaver": [
            {
              "id": 315987998,
              "tildeltEnhetsnr": "4833",
              "opprettetAvEnhetsnr": "4833",
              "journalpostId": "467045409",
              "beskrivelse": "Klage/anke",
              "temagruppe": "FMLI",
              "tema": "FOR",
              "oppgavetype": "JFR",
              "versjon": 1,
              "opprettetAv": "srvfpfordel",
              "prioritet": "NORM",
              "status": "OPPRETTET",
              "metadata": {},
              "fristFerdigstillelse": "2020-09-03",
              "aktivDato": "2020-09-02",
              "opprettetTidspunkt": "2020-09-02T14:23:48.956+02:00"
            }
          ]
        }
    """

    @Language("json")
    fun oppgaveResponseWithoutFrist() = """
        {
          "antallTreffTotalt": 1,
          "oppgaver": [
            {
              "id": 315987998,
              "tildeltEnhetsnr": "4833",
              "opprettetAvEnhetsnr": "4833",
              "journalpostId": "467045409",
              "beskrivelse": "Klage/anke",
              "temagruppe": "FMLI",
              "tema": "FOR",
              "oppgavetype": "JFR",
              "versjon": 1,
              "opprettetAv": "srvfpfordel",
              "prioritet": "NORM",
              "status": "OPPRETTET",
              "metadata": {},
              "fristFerdigstillelse": null,
              "aktivDato": "2020-09-02",
              "opprettetTidspunkt": "2020-09-02T14:23:48.956+02:00"
            }
          ]
        }
    """

    @Language("json")
    fun oppgaveResponseWithoutFristField() = """
        {
          "antallTreffTotalt": 1,
          "oppgaver": [
            {
              "id": 315987998,
              "tildeltEnhetsnr": "4833",
              "opprettetAvEnhetsnr": "4833",
              "journalpostId": "467045409",
              "beskrivelse": "Klage/anke",
              "temagruppe": "FMLI",
              "tema": "FOR",
              "oppgavetype": "JFR",
              "versjon": 1,
              "opprettetAv": "srvfpfordel",
              "prioritet": "NORM",
              "status": "OPPRETTET",
              "metadata": {},
              "aktivDato": "2020-09-02",
              "opprettetTidspunkt": "2020-09-02T14:23:48.956+02:00"
            }
          ]
        }
    """

}