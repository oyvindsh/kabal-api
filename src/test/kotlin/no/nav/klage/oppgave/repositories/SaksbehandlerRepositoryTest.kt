package no.nav.klage.oppgave.repositories

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.KlageProxyClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SaksbehandlerRepositoryTest {

    lateinit var klageProxyClient: KlageProxyClient

    lateinit var saksbehandlerRepository: SaksbehandlerRepository

    @BeforeEach
    fun setup() {
        klageProxyClient = mockk()

        every { klageProxyClient.getRoller("Z123456") } returns listOf("0000-GA-GOSYS_OPPGAVE_BEHANDLER")
        every { klageProxyClient.getRoller("Z654321") } returns listOf("anything")

        saksbehandlerRepository = SaksbehandlerRepository(mockk(), mockk(), klageProxyClient)
    }

    @Test
    fun `riktig rolle returnerer true`() {
        val erSaksbehandler = saksbehandlerRepository.erSaksbehandler("Z123456")
        assert(erSaksbehandler)
    }

    @Test
    fun `feil rolle returnerer false`() {
        val erSaksbehandler = saksbehandlerRepository.erSaksbehandler("Z654321")
        assert(!erSaksbehandler)
    }

}
