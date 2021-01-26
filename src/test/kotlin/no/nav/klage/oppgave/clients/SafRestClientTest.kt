package no.nav.klage.oppgave.clients

import brave.Tracer
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.clients.saf.rest.SafRestClient
import no.nav.klage.oppgave.service.TokenService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class SafRestClientTest {

    @MockK
    lateinit var tokenServiceMock: TokenService

    @MockK
    lateinit var tracerMock: Tracer

    @BeforeEach
    fun before() {
        every { tokenServiceMock.getStsSystembrukerToken() } returns "abc"
        every { tokenServiceMock.getSaksbehandlerAccessTokenWithSafScope() } returns "abc"
        every { tracerMock.currentSpan().context().traceIdString() } returns "def"
    }

    @Test
    fun `saf client fungerer som forventet`() {
        val dokument = getDokument(safResponse())
        assertThat(dokument.base64EncodedString).isNotNull
        val decodedBytes = Base64.getDecoder().decode(dokument.base64EncodedString)
        val decodedString = String(decodedBytes)
        assertThat(decodedString).isEqualTo("HEI PÅ DEG!")
    }

    fun getDokument(jsonResponse: String): ArkivertDokument {
        val safClient = SafRestClient(
            createShortCircuitWebClient(jsonResponse),
            tokenServiceMock,
            tracerMock
        )

        return safClient.getDokument("foo", "bar", "whatever")
    }

    fun safResponse() = "SEVJIFDDhSBERUch"
}