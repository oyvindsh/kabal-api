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
        return Endringslogginnslag.opprettingFromKabal(saksbehandlerident, "grunn", grunn.navn, this.id)
    }

    fun Klagebehandling.setKvalitetsvurderingEoes(eoes: Eoes, saksbehandlerident: String): Endringslogginnslag {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        kvalitetsvurdering!!.eoes = eoes
        kvalitetsvurdering!!.modified = LocalDateTime.now()
        return Endringslogginnslag.opprettingFromKabal(saksbehandlerident, "eøs", eoes.navn, this.id)
    }


}