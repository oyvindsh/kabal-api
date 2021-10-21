package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.view.FnrSearchResponse
import no.nav.klage.oppgave.api.view.KlagebehandlingListView
import no.nav.klage.oppgave.api.view.PersonSoekPersonView
import no.nav.klage.oppgave.clients.pdl.Sivilstand
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.MedunderskriverFlyt
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.personsoek.PersonSoekResponse
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class KlagebehandlingListMapper {

    //FIXME remove when not in use
    fun mapPersonSoekResponseToPersonSoekListView(
        personSoekResponse: List<PersonSoekResponse>,
        viseUtvidet: Boolean,
        saksbehandler: String?,
        tilgangTilTemaer: List<Tema>
    ): List<PersonSoekPersonView> {
        return if (personSoekResponse.size == 1) {
            personSoekResponse.map { person ->
                val klagebehandlinger =
                    mapEsKlagebehandlingerToListView(
                        esKlagebehandlinger = person.klagebehandlinger,
                        viseUtvidet = viseUtvidet,
                        viseFullfoerte = true,
                        saksbehandler = saksbehandler,
                        tilgangTilTemaer = tilgangTilTemaer
                    )
                PersonSoekPersonView(
                    fnr = person.fnr,
                    navn = person.navn,
                    foedselsdato = person.foedselsdato,
                    klagebehandlinger = klagebehandlinger,
                    aapneKlagebehandlinger = klagebehandlinger.filter { !it.isAvsluttetAvSaksbehandler },
                    avsluttedeKlagebehandlinger = klagebehandlinger.filter { it.isAvsluttetAvSaksbehandler }
                )
            }
        } else {
            personSoekResponse.map { person ->
                PersonSoekPersonView(
                    fnr = person.fnr,
                    navn = person.navn,
                    foedselsdato = person.foedselsdato
                )
            }
        }
    }

    fun mapPersonSoekHitsToFnrSearchResponse(
        personSoekHits: List<PersonSoekResponse>,
        saksbehandler: String?,
        tilgangTilTemaer: List<Tema>
    ): FnrSearchResponse? {
        return if (personSoekHits.size == 1) {
            val person = personSoekHits.first()
            val klagebehandlinger =
                mapEsKlagebehandlingerToListView(
                    esKlagebehandlinger = person.klagebehandlinger,
                    viseUtvidet = false,
                    viseFullfoerte = true,
                    saksbehandler = saksbehandler,
                    tilgangTilTemaer = tilgangTilTemaer
                )
            FnrSearchResponse(
                fnr = person.fnr,
                name = person.navn ?: throw RuntimeException("name missing"),
                klagebehandlinger = klagebehandlinger,
                aapneKlagebehandlinger = klagebehandlinger.filter { !it.isAvsluttetAvSaksbehandler },
                avsluttedeKlagebehandlinger = klagebehandlinger.filter { it.isAvsluttetAvSaksbehandler }
            )
        } else {
            throw RuntimeException("more than one hit for fnr")
        }
    }

    fun mapEsKlagebehandlingerToListView(
        esKlagebehandlinger: List<EsKlagebehandling>,
        viseUtvidet: Boolean,
        viseFullfoerte: Boolean,
        saksbehandler: String?,
        tilgangTilTemaer: List<Tema>,
        sivilstand: Sivilstand? = null
    ): List<KlagebehandlingListView> {
        return esKlagebehandlinger.map { esKlagebehandling ->
            KlagebehandlingListView(
                id = esKlagebehandling.id,
                person = if (viseUtvidet) {
                    KlagebehandlingListView.Person(
                        esKlagebehandling.sakenGjelderFnr,
                        esKlagebehandling.sakenGjelderNavn,
                        if (esKlagebehandling.sakenGjelderFnr == sivilstand?.foedselsnr) sivilstand?.type?.id else null
                    )
                } else {
                    null
                },
                type = esKlagebehandling.type,
                tema = esKlagebehandling.tema,
                hjemmel = esKlagebehandling.hjemler.firstOrNull(),
                frist = esKlagebehandling.frist,
                mottatt = esKlagebehandling.mottattKlageinstans.toLocalDate(),
                harMedunderskriver = esKlagebehandling.medunderskriverident != null,
                erMedunderskriver = esKlagebehandling.medunderskriverident != null && esKlagebehandling.medunderskriverident == saksbehandler,
                medunderskriverident = esKlagebehandling.medunderskriverident,
                medunderskriverFlyt = MedunderskriverFlyt.valueOf(esKlagebehandling.medunderskriverFlyt),
                erTildelt = esKlagebehandling.tildeltSaksbehandlerident != null,
                tildeltSaksbehandlerident = esKlagebehandling.tildeltSaksbehandlerident,
                tildeltSaksbehandlerNavn = esKlagebehandling.tildeltSaksbehandlernavn,
                utfall = if (viseFullfoerte) {
                    esKlagebehandling.vedtakUtfall
                } else {
                    null
                },
                avsluttetAvSaksbehandlerDate = if (viseFullfoerte) {
                    esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate()
                } else {
                    null
                },
                isAvsluttetAvSaksbehandler = esKlagebehandling.avsluttetAvSaksbehandler?.toLocalDate() != null,
                saksbehandlerHarTilgang = tilgangTilTemaer.contains(Tema.of(esKlagebehandling.tema)),
                egenAnsatt = esKlagebehandling.egenAnsatt,
                fortrolig = esKlagebehandling.fortrolig,
                strengtFortrolig = esKlagebehandling.strengtFortrolig,
                ageKA = esKlagebehandling.mottattKlageinstans.toAgeInDays()
            )
        }
    }

    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()
}