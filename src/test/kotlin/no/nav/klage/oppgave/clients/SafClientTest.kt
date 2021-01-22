package no.nav.klage.oppgave.clients

import brave.Tracer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.oppgave.clients.saf.DokumentoversiktBrukerResponse
import no.nav.klage.oppgave.clients.saf.SafClient
import no.nav.klage.oppgave.service.TokenService
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SafClientTest {

    @MockK
    lateinit var tokenServiceMock: TokenService

    @MockK
    lateinit var tracerMock: Tracer

    @BeforeEach
    fun before() {
        every { tokenServiceMock.getStsSystembrukerToken() } returns "abc"
        every { tokenServiceMock.getSaksbehandlerAccessTokenWithOppgaveScope() } returns "abc"
        every { tracerMock.currentSpan().context().traceIdString() } returns "def"
    }

    @Test
    fun `saf response kan mappes selv om ikke alle felt er med i kotlin`() {
        val dokumentoversiktBrukerResponse = getDokumentoversiktBruker(safResponse())
        assertThat(dokumentoversiktBrukerResponse.data).isNotNull
    }

    fun getDokumentoversiktBruker(jsonResponse: String): DokumentoversiktBrukerResponse {
        val safClient = SafClient(
            createShortCircuitWebClient(jsonResponse),
            tokenServiceMock,
            tracerMock
        )

        return safClient.getDokumentoversiktBruker("fnr", 1, null)
    }

    @Language("json")
    fun safResponse() = """
        {
          "data": {
            "dokumentoversiktBruker": {
              "journalposter": [
                {
                  "journalpostId": "492330029",
                  "tittel": "MASKERT_FELT",
                  "journalposttype": "I",
                  "journalstatus": "JOURNALFOERT",
                  "tema": "SYK",
                  "temanavn": "Sykepenger",
                  "behandlingstema": null,
                  "behandlingstemanavn": null,
                  "sak": {
                    "datoOpprettet": "2020-09-11T07:40:25",
                    "fagsakId": null,
                    "fagsaksystem": "FS22"
                  },
                  "bruker": {
                    "id": "1000034467123",
                    "type": "AKTOERID"
                  },
                  "avsenderMottaker": {
                    "id": "11126532778",
                    "type": "FNR",
                    "navn": "MASKERT_FELT",
                    "land": null,
                    "erLikBruker": true
                  },
                  "journalfoerendeEnhet": "9999",
                  "journalfortAvNavn": "Syfogsak",
                  "opprettetAvNavn": "Syfogsak",
                  "kanal": "NAV_NO",
                  "kanalnavn": "Ditt NAV",
                  "skjerming": null,
                  "datoOpprettet": "2020-12-14T12:22:03",
                  "relevanteDatoer": [
                    {
                      "dato": "2020-12-14T12:22:03",
                      "datotype": "DATO_DOKUMENT"
                    },
                    {
                      "dato": "2020-12-14T12:22:03",
                      "datotype": "DATO_JOURNALFOERT"
                    },
                    {
                      "dato": "2020-12-14T12:22:03",
                      "datotype": "DATO_REGISTRERT"
                    }
                  ],
                  "antallRetur": null,
                  "eksternReferanseId": null,
                  "tilleggsopplysninger": [],
                  "dokumenter": [
                    {
                      "dokumentInfoId": "514037527",
                      "tittel": "MASKERT_FELT",
                      "brevkode": "NAV 08-07.04 D",
                      "dokumentstatus": null,
                      "datoFerdigstilt": null,
                      "originalJournalpostId": "492330029",
                      "skjerming": null,
                      "dokumentvarianter": [
                        {
                          "variantformat": "ARKIV",
                          "filnavn": "Søknad om sykepenger 16.11.2020 - 10.12.2020",
                          "saksbehandlerHarTilgang": false,
                          "skjerming": null
                        }
                      ]
                    }
                  ]
                }
              ],
              "sideInfo": {
                "sluttpeker": "NDkyMzMwMDI5",
                "finnesNesteSide": true
              }
            }
          }
        }
    """


}