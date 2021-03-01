package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.KlagebehandlingListView
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class KlagebehandlingMapper(

) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        fetchPersoner: Boolean
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (fetchPersoner) {
                    KlagebehandlingListView.Person(
                        esKlagebehandling.foedselsnummer,
                        esKlagebehandling.navn
                    )
                } else {
                    null
                },
                type = esKlagebehandling.sakstype.id,
                tema = esKlagebehandling.tema.id,
                hjemmel = esKlagebehandling.hjemler?.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans,
                versjon = esKlagebehandling.versjon!!.toInt()
            )
        }
    }

    fun mapKlagebehandlingToKlagebehandlingView(klagebehandling: Klagebehandling): KlagebehandlingView {


        //TODO: Trenger vi egentlig Mottak her også?
        //Skal vi vise frem dataene vi mottok fra førsteinstans i tillegg til det saksbehandler har satt/endret?
        return KlagebehandlingView(
            id = klagebehandling.id,
            klageInnsendtdato = klagebehandling.innsendt,
            //TODO get name from norg2
            fraNAVEnhet = klagebehandling.avsenderEnhetFoersteinstans,
            fraSaksbehandlerident = klagebehandling.avsenderSaksbehandleridentFoersteinstans,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            foedselsnummer = klagebehandling.foedselsnummer ?: "mangler",
            tema = klagebehandling.tema.id,
            sakstype = klagebehandling.sakstype.navn,
            mottatt = klagebehandling.mottattKlageinstans,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            hjemler = hjemmelToHjemmelView(klagebehandling.hjemler),
            modified = klagebehandling.modified,
            created = klagebehandling.created
        )
    }

    private fun hjemmelToHjemmelView(hjemler: List<Hjemmel>): List<KlagebehandlingView.Hjemmel> {
        return hjemler.map {
            KlagebehandlingView.Hjemmel(
                kapittel = it.kapittel,
                paragraf = it.paragraf,
                ledd = it.ledd,
                bokstav = it.bokstav,
                original = it.original
            )
        }
    }

    fun mapKlagebehandlingToKvalitetsvurderingView(klagebehandling: Klagebehandling): KvalitetsvurderingView {
        return KvalitetsvurderingView(
            grunn = klagebehandling.kvalitetsvurdering?.grunn?.id,
            eoes = klagebehandling.kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = klagebehandling.kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = klagebehandling.kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = klagebehandling.kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = klagebehandling.kvalitetsvurdering?.tilbakemelding
        )
    }
}
