package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Eoes
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.RaadfoertMedLege
import no.nav.klage.oppgave.events.KlagebehandlingEndretEvent
import java.time.LocalDateTime

object KlagebehandlingAggregatFunctions {

    fun Klagebehandling.setTildeltSaksbehandlerident(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = tildeltSaksbehandlerident
        val tidspunkt = LocalDateTime.now()
        tildeltSaksbehandlerident = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_SAKSBEHANDLERIDENT, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setTildeltEnhet(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        val gammelVerdi = tildeltEnhet
        val tidspunkt = LocalDateTime.now()
        tildeltEnhet = nyVerdi
        modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_SAKSBEHANDLERIDENT, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingGrunn(
        nyVerdi: Grunn?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.grunn
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.grunn = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingEoes(
        nyVerdi: Eoes?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.eoes
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.eoes = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingRaadfoertMedLege(
        nyVerdi: RaadfoertMedLege?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.raadfoertMedLege
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.raadfoertMedLege = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.id.toString(),
                nyVerdi?.id.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingInternvurdering(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.internVurdering
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.internVurdering = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_SAKSBEHANDLERIDENT, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingSendTilbakemelding(
        nyVerdi: Boolean?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.sendTilbakemelding
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.sendTilbakemelding = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(
                saksbehandlerident,
                Felt.TILDELT_SAKSBEHANDLERIDENT,
                gammelVerdi?.toString(),
                nyVerdi?.toString(),
                tidspunkt
            )
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    fun Klagebehandling.setKvalitetsvurderingTilbakemelding(
        nyVerdi: String?,
        saksbehandlerident: String
    ): KlagebehandlingEndretEvent {
        if (kvalitetsvurdering == null) {
            kvalitetsvurdering = Kvalitetsvurdering()
        }
        val gammelVerdi = kvalitetsvurdering!!.tilbakemelding
        val tidspunkt = LocalDateTime.now()
        kvalitetsvurdering!!.tilbakemelding = nyVerdi
        kvalitetsvurdering!!.modified = tidspunkt
        val endringslogg =
            endringslogg(saksbehandlerident, Felt.TILDELT_SAKSBEHANDLERIDENT, gammelVerdi, nyVerdi, tidspunkt)
        return KlagebehandlingEndretEvent(klagebehandling = this, endringslogginnslag = listOfNotNull(endringslogg))
    }

    private fun Klagebehandling.endringslogg(
        saksbehandlerident: String,
        felt: Felt,
        fraVerdi: String?,
        tilVerdi: String?,
        tidspunkt: LocalDateTime
    ): Endringslogginnslag? {
        return Endringslogginnslag.endringslogg(
            saksbehandlerident,
            felt,
            fraVerdi,
            tilVerdi,
            this.id,
            tidspunkt
        )
    }

}