package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikOversendelsesbrev
import no.nav.klage.oppgave.exceptions.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class KvalitetsvurderingTest {

    @Test
    fun `riktig utfylt kvalitetsvurdering passerer`() {
        val kvalitetsvurdering = Kvalitetsvurdering(
            oversendelsesbrevBra = true,
            utredningBra = true,
            vedtakBra = true
        )
        kvalitetsvurdering.validate()
        assert(true)
    }

    @Test
    fun `tom kvalitetsvurdering feiler`() {
        val kvalitetsvurdering = Kvalitetsvurdering()
        assertThrows<ValidationException> { kvalitetsvurdering.validate() }
    }

    @Test
    fun `manglende avviksliste ved false i kvalitetsvurdering feiler`() {
        val kvalitetsvurdering = Kvalitetsvurdering(
            oversendelsesbrevBra = false
        )
        assertThrows<ValidationException> { kvalitetsvurdering.validate() }
    }

    @Test
    fun `utfylt avviksliste ved false i kvalitetsvurdering passerer`() {
        val kvalitetsvurdering = Kvalitetsvurdering(
            oversendelsesbrevBra = false,
            kvalitetsavvikOversendelsesbrev = mutableSetOf(KvalitetsavvikOversendelsesbrev.HOVEDINNHOLDET_IKKE_GJENGITT),
            utredningBra = true,
            vedtakBra = true
        )
        kvalitetsvurdering.validate()
        assert(true)
    }
}