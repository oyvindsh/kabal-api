package no.nav.klage.oppgave.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FnrUtilTest {

    @Test
    fun `ugyldig fødselsnummer gir feil`() {
        assertThat(isValidFnr("12345678910")).isFalse
    }

    @Test
    fun `gyldig fødselsnummer gir rett svar`() {
        assertThat(isValidFnr("02446701749")).isTrue
    }

}
