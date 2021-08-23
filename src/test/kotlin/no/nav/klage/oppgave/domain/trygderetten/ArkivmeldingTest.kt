package no.nav.klage.oppgave.domain.trygderetten

import org.junit.jupiter.api.Test

internal class ArkivmeldingTest {

    @Test
    fun `create xml`() {
        val arkivmelding = Arkivmelding(1, "John", "Doe", "555-555-5555")
        println(arkivmelding.toXml())
    }

}