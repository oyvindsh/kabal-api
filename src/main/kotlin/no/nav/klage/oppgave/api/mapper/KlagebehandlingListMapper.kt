package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.KlagebehandlingListView
import no.nav.klage.oppgave.api.view.PersonSoekPersonView
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class KlagebehandlingListMapper() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapEsKlagebehandlingerToPersonListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        viseUtvidet: Boolean,
        saksbehandler: String?,
        tilgangTilTemaer: List<Tema>
    ): List<PersonSoekPersonView> {
        return esKlagebehandlinger.groupBy { it.sakenGjelderFnr }.map { (key, value) ->
            val klagebehandlinger =
                mapEsKlagebehandlingerToListView(value, viseUtvidet, true, saksbehandler, tilgangTilTemaer)
            PersonSoekPersonView(
                fnr = key!!,
                navn = value.first().sakenGjelderNavn,
                klagebehandlinger = klagebehandlinger,
                aapneKlagebehandlinger = klagebehandlinger.filter { it.avsluttetAvSaksbehandler == null },
                avsluttedeKlagebehandlinger = klagebehandlinger.filter { it.avsluttetAvSaksbehandler != null }
            )
        }
    }

    fun mapEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        viseUtvidet: Boolean,
        viseFullfoerte: Boolean,
        saksbehandler: String?,
        tilgangTilTemaer: List<Tema>
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (viseUtvidet) {
                    KlagebehandlingListView.Person(
                        esKlagebehandling.sakenGjelderFnr,
                        esKlagebehandling.sakenGjelderNavn
                    )
                } else {
                    null
                },
                type = esKlagebehandling.type,
                tema = esKlagebehandling.tema,
                hjemmel = esKlagebehandling.hjemler.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans?.toLocalDate(),
                versjon = esKlagebehandling.versjon!!.toInt(),
                klagebehandlingVersjon = esKlagebehandling.versjon,
                harMedunderskriver = esKlagebehandling.medunderskriverident != null,
                erMedunderskriver = esKlagebehandling.medunderskriverident != null && esKlagebehandling.medunderskriverident == saksbehandler,
                medunderskriverident = esKlagebehandling.medunderskriverident,
                erTildelt = esKlagebehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esKlagebehandling.tildeltSaksbehandlerident,
                utfall = if (viseFullfoerte) {
                    esKlagebehandling.vedtakUtfall
                } else {
                    null
                },
                avsluttetAvSaksbehandler = if (viseFullfoerte) {
                    esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate()
                } else {
                    null
                },
                saksbehandlerHarTilgang = tilgangTilTemaer.contains(Tema.of(esKlagebehandling.tema))
            )
        }
    }
}

