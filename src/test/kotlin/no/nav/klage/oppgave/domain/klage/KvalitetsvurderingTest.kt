package no.nav.klage.oppgave.domain.klage

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikOversendelsesbrev
import no.nav.klage.oppgave.exceptions.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime

internal class KvalitetsvurderingTest {

    @Test
    fun `riktig utfylt kvalitetsvurdering passerer`() {
        val kvalitetsvurdering = Kvalitetsvurdering(
            oversendelsesbrevBra = true,
            utredningBra = true,
            vedtakBra = true
        )
        kvalitetsvurdering.validate()
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
    }

    @Test
    fun `test boolean`() {

        val nullValue: LocalDateTime? = null
        val notNullValue = LocalDateTime.now()

        val data = Data(
            isAvsluttetAvSaksbehandler = nullValue?.toLocalDate() != null
        )

        println(jacksonObjectMapper().writeValueAsString(data))

        println(data)
    }

    private data class Data(val isAvsluttetAvSaksbehandler: Boolean)

}