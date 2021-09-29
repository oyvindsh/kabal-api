package no.nav.klage.oppgave.repositories

import io.mockk.every
import io.mockk.mockk
import no.nav.klage.oppgave.clients.axsys.AxsysGateway
import no.nav.klage.oppgave.clients.axsys.Tilganger
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.gateway.IAzureGateway
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test

internal class SaksbehandlerInfoRepositoryTest {

    private val axsysGateway: AxsysGateway = mockk()
    private val msClient: IAzureGateway = mockk()
    private val tilganger = Tilganger(emptyList())

    private val repo: SaksbehandlerRepository =
        SaksbehandlerRepository(msClient, axsysGateway, "", "", "", "", "", "", "", "")


    @Test
    fun harTilgangTilEnhetOgTema() {
        every { axsysGateway.getEnheterMedTemaerForSaksbehandler("01010112345") } returns EnheterMedLovligeTemaer(
            listOf(
                EnhetMedLovligeTemaer("4290", "KA Nord", listOf(Tema.SYK, Tema.OMS, Tema.AAP)),
                EnhetMedLovligeTemaer("4291", "KA Sør", listOf(Tema.OMS, Tema.AAP, Tema.DAG))
            )
        )

        val softly = SoftAssertions()
        softly.assertThat(repo.harTilgangTilEnhetOgTema("01010112345", "4290", Tema.SYK)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhetOgTema("01010112345", "4290", Tema.OMS)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhetOgTema("01010112345", "4290", Tema.DAG)).isEqualTo(false)
        softly.assertThat(repo.harTilgangTilEnhetOgTema("01010112345", "4291", Tema.DAG)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhetOgTema("01010112345", "4291", Tema.OMS)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhetOgTema("01010112345", "4291", Tema.SYK)).isEqualTo(false)
        softly.assertAll()
    }

    @Test
    fun harTilgangTilEnhet() {
        every { axsysGateway.getEnheterMedTemaerForSaksbehandler("01010112345") } returns EnheterMedLovligeTemaer(
            listOf(
                EnhetMedLovligeTemaer("4290", "KA Nord", listOf(Tema.SYK, Tema.OMS, Tema.AAP)),
                EnhetMedLovligeTemaer("4291", "KA Sør", listOf(Tema.OMS, Tema.AAP, Tema.DAG))
            )
        )

        val softly = SoftAssertions()
        softly.assertThat(repo.harTilgangTilEnhet("01010112345", "4290")).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilEnhet("01010112345", "4291")).isEqualTo(true)
        softly.assertAll()
    }

    @Test
    fun harTilgangTilTema() {
        every { axsysGateway.getEnheterMedTemaerForSaksbehandler("01010112345") } returns EnheterMedLovligeTemaer(
            listOf(
                EnhetMedLovligeTemaer("4290", "KA Nord", listOf(Tema.SYK, Tema.OMS, Tema.AAP)),
                EnhetMedLovligeTemaer("4291", "KA Sør", listOf(Tema.OMS, Tema.AAP, Tema.DAG))
            )
        )

        val softly = SoftAssertions()
        softly.assertThat(repo.harTilgangTilTema("01010112345", Tema.SYK)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilTema("01010112345", Tema.OMS)).isEqualTo(true)
        softly.assertThat(repo.harTilgangTilTema("01010112345", Tema.DAG)).isEqualTo(true)
        softly.assertAll()
    }
}