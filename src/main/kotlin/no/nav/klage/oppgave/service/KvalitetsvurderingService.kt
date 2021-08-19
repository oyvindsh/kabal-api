package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setBrukSomEksempelIOpplaering
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingAvvikStorKonsekvens
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingKommentarOversendelsesbrev
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingKommentarUtredning
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingKommentarVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingKvalitetsavvikOversendelsesbrev
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingKvalitetsavvikUtredning
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingKvalitetsavvikVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingOversendelsesbrevBra
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingUtredningBra
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingVedtakBra
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikOversendelsesbrev
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikUtredning
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikVedtak
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class KvalitetsvurderingService(
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    fun setOversendelsesbrevBra(
        klagebehandling: Klagebehandling,
        oversendelsesbrevBra: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingOversendelsesbrevBra(oversendelsesbrevBra, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsavvikOversendelsesbrev(
        klagebehandling: Klagebehandling,
        kvalitetsavvikOversendelsesbrev: Set<KvalitetsavvikOversendelsesbrev>,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingKvalitetsavvikOversendelsesbrev(
            kvalitetsavvikOversendelsesbrev,
            saksbehandlerIdent
        )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKommentarOversendelsesbrev(
        klagebehandling: Klagebehandling,
        kommentarOversendelsesbrev: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingKommentarOversendelsesbrev(
            kommentarOversendelsesbrev,
            saksbehandlerIdent
        )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setUtredningBra(
        klagebehandling: Klagebehandling,
        utredningBra: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingUtredningBra(utredningBra, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsavvikUtredning(
        klagebehandling: Klagebehandling,
        kvalitetsavvikUtredning: Set<KvalitetsavvikUtredning>,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingKvalitetsavvikUtredning(
            kvalitetsavvikUtredning,
            saksbehandlerIdent
        )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKommentarUtredning(
        klagebehandling: Klagebehandling,
        kommentarUtredning: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingKommentarUtredning(kommentarUtredning, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setVedtakBra(
        klagebehandling: Klagebehandling,
        vedtakBra: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingVedtakBra(vedtakBra, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsavvikVedtak(
        klagebehandling: Klagebehandling,
        kvalitetsavvikVedtak: Set<KvalitetsavvikVedtak>,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingKvalitetsavvikVedtak(kvalitetsavvikVedtak, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKommentarVedtak(
        klagebehandling: Klagebehandling,
        kommentarVedtak: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingKommentarVedtak(kommentarVedtak, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setAvvikStorKonsekvens(
        klagebehandling: Klagebehandling,
        avvikStorKonsekvens: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setKvalitetsvurderingAvvikStorKonsekvens(avvikStorKonsekvens, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setBrukSomEksempelIOpplaering(
        klagebehandling: Klagebehandling,
        brukSomEksempelIOpplaering: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val event = klagebehandling.setBrukSomEksempelIOpplaering(brukSomEksempelIOpplaering, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }
}