package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class KlagebehandlingMapper(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling: Klagebehandling): EsKlagebehandling {
        val foedselsnummer = foedselsnummer(klagebehandling)

        val personInfo = foedselsnummer?.let { pdlFacade.getPersonInfo(it) }
        val erFortrolig = personInfo?.harBeskyttelsesbehovFortrolig() ?: false
        val erStrengtFortrolig = personInfo?.harBeskyttelsesbehovStrengtFortrolig() ?: false
        val erEgenAnsatt = foedselsnummer?.let { egenAnsattService.erEgenAnsatt(it) } ?: false
        val navn = personInfo?.navn

        return EsKlagebehandling(
            id = klagebehandling.id.toString(),
            versjon = klagebehandling.versjon,
            journalpostId = klagebehandling.saksdokumenter.map { it.journalpostId },
            saksreferanse = klagebehandling.referanseId,
            tildeltEnhet = klagebehandling.tildeltEnhet,
            tema = klagebehandling.tema,
            sakstype = klagebehandling.sakstype,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            innsendt = klagebehandling.innsendt,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            mottattKlageinstans = klagebehandling.mottattKlageinstans,
            frist = klagebehandling.frist,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            hjemler = klagebehandling.hjemler.map { it.original },
            foedselsnummer = foedselsnummer,
            virksomhetsnummer = virksomhetsnummer(klagebehandling),
            navn = navn,
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig
        )
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
                versjon = esKlagebehandling.versjon!!.toInt(),
                klagebehandlingVersjon = esKlagebehandling.versjon

            )
        }
    }

    fun mapKlagebehandlingToKlagebehandlingView(klagebehandling: Klagebehandling): KlagebehandlingView {
        return KlagebehandlingView(
            id = klagebehandling.id,
            klageInnsendtdato = klagebehandling.innsendt,
            //TODO get name from norg2
            fraNAVEnhet = klagebehandling.avsenderEnhetFoersteinstans,
            fraSaksbehandlerident = klagebehandling.avsenderSaksbehandleridentFoersteinstans,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            foedselsnummer = foedselsnummer(klagebehandling),
            virksomhetsnummer = virksomhetsnummer(klagebehandling),
            tema = klagebehandling.tema.id,
            sakstype = klagebehandling.sakstype.navn,
            mottatt = klagebehandling.mottattKlageinstans,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            hjemler = hjemmelToHjemmelView(klagebehandling.hjemler),
            modified = klagebehandling.modified,
            created = klagebehandling.created,
            klagebehandlingVersjon = klagebehandling.versjon
        )
    }

    fun mapKlagebehandlingToKlagebehandlingDetaljerView(klagebehandling: Klagebehandling): KlagebehandlingDetaljerView {
        return KlagebehandlingDetaljerView(
            id = klagebehandling.id,
            klageInnsendtdato = klagebehandling.innsendt,
            //TODO get name from norg2
            fraNAVEnhet = klagebehandling.avsenderEnhetFoersteinstans,
            fraSaksbehandlerident = klagebehandling.avsenderSaksbehandleridentFoersteinstans,
            mottattFoersteinstans = klagebehandling.mottattFoersteinstans,
            sakenGjelderFoedselsnummer = klagebehandling.sakenGjelder,
            foedselsnummer = foedselsnummer(klagebehandling),
            virksomhetsnummer = virksomhetsnummer(klagebehandling),
            tema = klagebehandling.tema.id,
            sakstype = klagebehandling.sakstype.navn,
            mottatt = klagebehandling.mottattKlageinstans,
            startet = klagebehandling.startet,
            avsluttet = klagebehandling.avsluttet,
            frist = klagebehandling.frist,
            tildeltSaksbehandlerident = klagebehandling.tildeltSaksbehandlerident,
            hjemler = hjemmelToHjemmelView(klagebehandling.hjemler),
            modified = klagebehandling.modified,
            created = klagebehandling.created,
            grunn = klagebehandling.kvalitetsvurdering?.grunn?.id,
            eoes = klagebehandling.kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = klagebehandling.kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = klagebehandling.kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = klagebehandling.kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = klagebehandling.kvalitetsvurdering?.tilbakemelding,
            klagebehandlingVersjon = klagebehandling.versjon
        )
    }

    private fun hjemmelToHjemmelView(hjemler: Set<Hjemmel>): List<HjemmelView> {
        return hjemler.map {
            HjemmelView(
                kapittel = it.kapittel,
                paragraf = it.paragraf,
                ledd = it.ledd,
                bokstav = it.bokstav,
                original = it.original
            )
        }.sortedBy { it.original }
    }

    fun mapKlagebehandlingToKvalitetsvurderingView(klagebehandling: Klagebehandling): KvalitetsvurderingView {
        val kvalitetsvurdering = klagebehandling.kvalitetsvurdering
        return KvalitetsvurderingView(
            grunn = kvalitetsvurdering?.grunn?.id,
            eoes = kvalitetsvurdering?.eoes?.id,
            raadfoertMedLege = kvalitetsvurdering?.raadfoertMedLege?.id,
            internVurdering = kvalitetsvurdering?.internVurdering,
            sendTilbakemelding = kvalitetsvurdering?.sendTilbakemelding,
            tilbakemelding = kvalitetsvurdering?.tilbakemelding,
            klagebehandlingVersjon = klagebehandling.versjon
        )
    }

    private fun foedselsnummer(klagebehandling: Klagebehandling) =
        if (klagebehandling.klagerPart.erPerson()) {
            klagebehandling.klagerPart.partId.value
        } else {
            null
        }

    private fun virksomhetsnummer(klagebehandling: Klagebehandling) =
        if (klagebehandling.klagerPart.erVirksomhet()) {
            klagebehandling.klagerPart.partId.value
        } else {
            null
        }
}
