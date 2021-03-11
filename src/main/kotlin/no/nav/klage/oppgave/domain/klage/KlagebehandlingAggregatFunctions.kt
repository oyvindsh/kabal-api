package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import java.time.LocalDateTime

object KlagebehandlingAggregatFunctions {

    fun Klagebehandling.setKvalitetsvurderingGrunn(grunn: Grunn, saksbehandlerident: String): Endringslogginnslag {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        kvalitetsvurdering!!.grunn = grunn
        kvalitetsvurdering!!.modified = LocalDateTime.now()
        return Endringslogginnslag.opprettingFromKabal(
            saksbehandlerident = saksbehandlerident,
            felt = "grunn",
            tilVerdi = grunn.navn,
            klagebehandlingId = this.id
        )
    }

    fun Klagebehandling.setKvalitetsvurderingEoes(eoes: Eoes, saksbehandlerident: String): Endringslogginnslag {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        kvalitetsvurdering!!.eoes = eoes
        kvalitetsvurdering!!.modified = LocalDateTime.now()
        return Endringslogginnslag.opprettingFromKabal(
            saksbehandlerident = saksbehandlerident,
            felt = "eoes",
            tilVerdi = eoes.navn,
            klagebehandlingId = this.id
        )
    }


}