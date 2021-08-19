package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.KlagebehandlingKvalitetsvurderingView
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class KvalitetsvurderingMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapKlagebehandlingToKvalitetsvurderingView(klagebehandling: Klagebehandling): KlagebehandlingKvalitetsvurderingView {
        return KlagebehandlingKvalitetsvurderingView(
            klagebehandlingId = klagebehandling.id,
            klagebehandlingVersjon = klagebehandling.versjon,
            kvalitetOversendelsesbrevBra = klagebehandling.kvalitetsvurdering?.oversendelsesbrevBra,
            kvalitetsavvikOversendelsesbrev = klagebehandling.kvalitetsvurdering?.kvalitetsavvikOversendelsesbrev?.map {
                it.id
            }?.toSet() ?: emptySet(),
            kommentarOversendelsesbrev = klagebehandling.kvalitetsvurdering?.kommentarOversendelsesbrev,
            kvalitetUtredningBra = klagebehandling.kvalitetsvurdering?.utredningBra,
            kvalitetsavvikUtredning = klagebehandling.kvalitetsvurdering?.kvalitetsavvikUtredning?.map {
                it.id
            }?.toSet() ?: emptySet(),
            kommentarUtredning = klagebehandling.kvalitetsvurdering?.kommentarUtredning,
            kvalitetVedtakBra = klagebehandling.kvalitetsvurdering?.vedtakBra,
            kvalitetsavvikVedtak = klagebehandling.kvalitetsvurdering?.kvalitetsavvikVedtak?.map {
                it.id
            }?.toSet() ?: emptySet(),
            kommentarVedtak = klagebehandling.kvalitetsvurdering?.kommentarVedtak,
            avvikStorKonsekvens = klagebehandling.kvalitetsvurdering?.avvikStorKonsekvens,
            brukSomEksempelIOpplaering = klagebehandling.kvalitetsvurdering?.brukSomEksempelIOpplaering
        )
    }
}

