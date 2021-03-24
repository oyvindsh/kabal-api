package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.addSaksdokument
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.removeSaksdokument
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setAvsenderEnhetFoersteinstans
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setAvsenderSaksbehandleridentFoersteinstans
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setFrist
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setInnsendt
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingEoes
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingGrunn
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingInternvurdering
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingRaadfoertMedLege
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingSendTilbakemelding
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setKvalitetsvurderingTilbakemelding
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setMottattFoersteinstans
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setMottattKlageinstans
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setSakstype
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setTema
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setTildeltSaksbehandlerident
import no.nav.klage.oppgave.domain.klage.Kvalitetsvurdering
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class KlagebehandlingService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val tilgangService: TilgangService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val hjemmelService: HjemmelService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private fun checkTilgang(klagebehandling: Klagebehandling) {
        klagebehandling.foedselsnummer?.let {
            tilgangService.verifySaksbehandlersTilgangTil(it)
        }
    }

    fun getKlagebehandling(klagebehandlingId: UUID): Klagebehandling =
        klagebehandlingRepository.getOne(klagebehandlingId).also { checkTilgang(it) }

    fun getKvalitetsvurdering(klagebehandlingId: UUID): Kvalitetsvurdering? =
        klagebehandlingRepository.getOne(klagebehandlingId).also { checkTilgang(it) }.kvalitetsvurdering

    fun assignKlagebehandling(
        klagebehandlingId: UUID,
        tildeltSaksbehandlerIdent: String?,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setTildeltSaksbehandlerident(tildeltSaksbehandlerIdent, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setSakstype(
        klagebehandlingId: UUID,
        sakstype: Sakstype,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setSakstype(sakstype, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setTema(
        klagebehandlingId: UUID,
        tema: Tema,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setTema(tema, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setInnsendt(
        klagebehandlingId: UUID,
        innsendt: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setInnsendt(innsendt, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setMottattFoersteinstans(
        klagebehandlingId: UUID,
        mottattFoersteinstans: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setMottattFoersteinstans(mottattFoersteinstans, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setMottattKlageinstans(
        klagebehandlingId: UUID,
        mottattKlageinstans: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setMottattKlageinstans(mottattKlageinstans, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setFrist(
        klagebehandlingId: UUID,
        frist: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setFrist(frist, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setAvsenderSaksbehandleridentFoersteinstans(
        klagebehandlingId: UUID,
        saksbehandlerIdent: String,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setAvsenderSaksbehandleridentFoersteinstans(
                saksbehandlerIdent,
                utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setAvsenderEnhetFoersteinstans(
        klagebehandlingId: UUID,
        enhet: String,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event =
            klagebehandling.setAvsenderEnhetFoersteinstans(
                enhet,
                utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingGrunn(
        klagebehandlingId: UUID,
        grunn: Grunn?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event = klagebehandling.setKvalitetsvurderingGrunn(grunn, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingEoes(
        klagebehandlingId: UUID,
        eoes: Eoes?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event = klagebehandling.setKvalitetsvurderingEoes(eoes, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingRaadfoertMedLege(
        klagebehandlingId: UUID,
        raadfoertMedLege: RaadfoertMedLege?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event = klagebehandling.setKvalitetsvurderingRaadfoertMedLege(raadfoertMedLege, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingInternVurdering(
        klagebehandlingId: UUID,
        internVurdering: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event = klagebehandling.setKvalitetsvurderingInternvurdering(internVurdering, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingSendTilbakemelding(
        klagebehandlingId: UUID,
        sendTilbakemelding: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event = klagebehandling.setKvalitetsvurderingSendTilbakemelding(sendTilbakemelding, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingTilbakemelding(
        klagebehandlingId: UUID,
        tilbakemelding: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        val event = klagebehandling.setKvalitetsvurderingTilbakemelding(tilbakemelding, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun createKlagebehandlingFromMottak(mottak: Mottak) {
        if (klagebehandlingRepository.findByMottakId(mottak.id) != null) {
            logger.error("We already have a klagebehandling for mottak ${mottak.id}, will not create a new one, nor update the existing one.")
        }
        val klagebehandling = klagebehandlingRepository.save(
            Klagebehandling(
                foedselsnummer = mottak.foedselsnummer,
                tema = mottak.tema,
                sakstype = mottak.sakstype,
                referanseId = mottak.referanseId,
                innsendt = null,
                mottattFoersteinstans = null,
                avsenderEnhetFoersteinstans = mottak.avsenderEnhet,
                avsenderSaksbehandleridentFoersteinstans = mottak.avsenderSaksbehandlerident,
                mottattKlageinstans = mottak.oversendtKaDato,
                startet = null,
                avsluttet = null,
                frist = mottak.fristFraFoersteinstans,
                tildeltSaksbehandlerident = mottak.tildeltSaksbehandlerident,
                tildeltEnhet = mottak.tildeltEnhet,
                mottakId = mottak.id,
                vedtak = mutableSetOf(),
                kvalitetsvurdering = null,
                hjemler = mottak.hjemler().map { hjemmelService.generateHjemmelFromText(it) }.toMutableSet(),
                //TODO lookup actual documents and link them
//                saksdokumenter = if (mottak.journalpostId != null) {
//                    mutableSetOf(Saksdokument(journalpostId = mottak.journalpostId!!))
//                } else {
//                    mutableSetOf()
//                },
                kilde = mottak.kilde
            )
        )
        logger.debug("Created behandling ${klagebehandling.id} for mottak ${mottak.id}")
        applicationEventPublisher.publishEvent(
            KlagebehandlingEndretEvent(
                klagebehandling = klagebehandling,
                endringslogginnslag = emptyList()
            )
        )
    }


    private fun mapSakstype(behandlingstype: String): Sakstype = Sakstype.of(behandlingstype)

    private fun mapTema(tema: String): Tema = Tema.of(tema)

    fun addDokument(
        klagebehandlingId: UUID,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        try {
            if (klagebehandling.saksdokumenter.any { it.journalpostId == journalpostId }) {
                logger.debug("Journalpost $journalpostId is already connected to klagebehandling $klagebehandlingId, doing nothing")
            } else {
                val event =
                    klagebehandling.addSaksdokument(
                        Saksdokument(
                            journalpostId = journalpostId,
                            dokumentInfoId = dokumentInfoId
                        ), saksbehandlerIdent
                    )
                event?.let { applicationEventPublisher.publishEvent(it) }
            }
        } catch (e: Exception) {
            logger.error("Error connecting journalpost $journalpostId to klagebehandling $klagebehandlingId", e)
            throw e
        }
    }

    fun removeDokument(
        klagebehandlingId: UUID,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        try {
            if (klagebehandling.saksdokumenter.none { it.journalpostId == journalpostId && it.dokumentInfoId == dokumentInfoId }) {
                logger.debug("Journalpost $journalpostId is not connected to klagebehandling $klagebehandlingId, doing nothing")
            } else {
                val event =
                    klagebehandling.removeSaksdokument(
                        Saksdokument(
                            journalpostId = journalpostId,
                            dokumentInfoId = dokumentInfoId
                        ), saksbehandlerIdent
                    )
                event?.let { applicationEventPublisher.publishEvent(it) }
            }
        } catch (e: Exception) {
            logger.error("Error disconnecting journalpost $journalpostId to klagebehandling $klagebehandlingId", e)
            throw e
        }
    }

    /*
    //Oppgaven kan ha gått ping-pong frem og tilbake, så det vi leter etter her er siste gang den ble assignet KA
    private fun findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopiVersjoner: List<OppgaveKopiVersjon>): OppgaveKopiVersjon? =
        oppgaveKopiVersjoner.zipWithNext()
            .firstOrNull {
                it.first.tildeltEnhetsnr.startsWith(KLAGEINSTANS_PREFIX)
                        && !it.second.tildeltEnhetsnr.startsWith(KLAGEINSTANS_PREFIX)
            }
            ?.first
    fun connectOppgaveKopiToKlagebehandling(oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>) {
        val lastVersjon = oppgaveKopierOrdererByVersion.first()

        if (lastVersjon.tildeltEnhetsnr.startsWith(KLAGEINSTANS_PREFIX) && (lastVersjon.oppgavetype == "BEH_SAK_MK" || lastVersjon.oppgavetype == "BEH_SAK")) {
            val mottakSomHarPaagaaendeKlagebehandlinger =
                fetchMottakForOppgaveKopi(lastVersjon.id).filter {
                    klagebehandlingRepository.findByMottakId(it.id)?.avsluttet == null
                }
            val klagebehandlingerOgMottak = if (mottakSomHarPaagaaendeKlagebehandlinger.isEmpty()) {
                listOf(createNewMottakAndKlagebehandling(oppgaveKopierOrdererByVersion))
            } else {
                mottakSomHarPaagaaendeKlagebehandlinger.map { updateMottak(it, oppgaveKopierOrdererByVersion) }
            }
            klagebehandlingerOgMottak.map { KlagebehandlingEndretEvent(it.first, emptyList()) }
                .forEach { applicationEventPublisher.publishEvent(it) }
        }
    }

    private fun createNewMottakAndKlagebehandling(oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>): Pair<Klagebehandling, Mottak> {
        val lastVersjon = oppgaveKopierOrdererByVersion.first()
        requireNotNull(lastVersjon.ident)
        requireNotNull(lastVersjon.behandlingstype)

        val overfoeringsdata = overfoeringsdataParserService.parseBeskrivelse(lastVersjon.beskrivelse ?: "")

        val createdMottak = mottakRepository.save(
            Mottak(
                tema = mapTema(lastVersjon.tema),
                sakstype = mapSakstype(lastVersjon.behandlingstype),
                referanseId = lastVersjon.saksreferanse,
                foedselsnummer = lastVersjon.ident.folkeregisterident,
                organisasjonsnummer = mapOrganisasjonsnummer(lastVersjon.ident),
                hjemmelListe = mapHjemler(lastVersjon),
                avsenderSaksbehandlerident = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.endretAv
                    ?: overfoeringsdata?.saksbehandlerWhoMadeTheChange,
                avsenderEnhet = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.endretAvEnhetsnr
                    ?: overfoeringsdata?.enhetOverfoertFra ?: lastVersjon.opprettetAvEnhetsnr,
                oversendtKaEnhet = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.tildeltEnhetsnr
                    ?: overfoeringsdata?.enhetOverfoertTil ?: lastVersjon.tildeltEnhetsnr,
                oversendtKaDato = findFirstVersionWhereTildeltEnhetIsKA(oppgaveKopierOrdererByVersion)?.endretTidspunkt?.toLocalDate()
                    ?: overfoeringsdata?.datoForOverfoering ?: lastVersjon.opprettetTidspunkt.toLocalDate(),
                fristFraFoersteinstans = lastVersjon.fristFerdigstillelse,
                beskrivelse = lastVersjon.beskrivelse,
                status = lastVersjon.status.name,
                statusKategori = lastVersjon.statuskategori().name,
                tildeltEnhet = lastVersjon.tildeltEnhetsnr,
                tildeltSaksbehandlerident = lastVersjon.tilordnetRessurs,
                journalpostId = lastVersjon.journalpostId,
                journalpostKilde = lastVersjon.journalpostkilde,
                kilde = Kilde.OPPGAVE,
                oppgavereferanser = mutableListOf(
                    Oppgavereferanse(
                        oppgaveId = lastVersjon.id
                    )
                )
            )
        )

        val createdKlagebehandling = klagebehandlingRepository.save(
            Klagebehandling(
                foedselsnummer = createdMottak.foedselsnummer,
                tema = createdMottak.tema,
                sakstype = createdMottak.sakstype,
                referanseId = createdMottak.referanseId,
                innsendt = null,
                mottattFoersteinstans = null,
                avsenderEnhetFoersteinstans = createdMottak.avsenderEnhet,
                avsenderSaksbehandleridentFoersteinstans = createdMottak.avsenderSaksbehandlerident,
                mottattKlageinstans = createdMottak.oversendtKaDato,
                startet = null,
                avsluttet = null,
                frist = createdMottak.fristFraFoersteinstans,
                tildeltSaksbehandlerident = createdMottak.tildeltSaksbehandlerident,
                tildeltEnhet = createdMottak.tildeltEnhet,
                mottakId = createdMottak.id,
                vedtak = mutableSetOf(),
                kvalitetsvurdering = null,
                hjemler = createdMottak.hjemler().map { hjemmelService.generateHjemmelFromText(it) }.toMutableSet(),
                saksdokumenter = if (createdMottak.journalpostId != null) {
                    mutableSetOf(Saksdokument(journalpostId = createdMottak.journalpostId!!))
                } else {
                    mutableSetOf()
                },
                kilde = Kilde.OPPGAVE
            )
        )
        logger.debug("Created behandling ${createdKlagebehandling.id} with mottak ${createdMottak.id} for oppgave ${lastVersjon.id}")
        return Pair(createdKlagebehandling, createdMottak)
    }


    private fun updateMottak(
        mottak: Mottak,
        oppgaveKopierOrdererByVersion: List<OppgaveKopiVersjon>
    ): Pair<Klagebehandling, Mottak> {
        logger.debug("Updating mottak")

        val lastVersjon = oppgaveKopierOrdererByVersion.first()
        requireNotNull(lastVersjon.ident)
        requireNotNull(lastVersjon.behandlingstype)

        //TODO: Legge til nytt saksdokument hvis journalpostId er oppdatert?
        mottak.apply {
            tema = mapTema(lastVersjon.tema)
            sakstype = mapSakstype(lastVersjon.behandlingstype)
            referanseId = lastVersjon.saksreferanse
            foedselsnummer = lastVersjon.ident.folkeregisterident
            organisasjonsnummer = mapOrganisasjonsnummer(lastVersjon.ident)
            hjemmelListe = mapHjemler(lastVersjon)
            fristFraFoersteinstans = lastVersjon.fristFerdigstillelse
            beskrivelse = lastVersjon.beskrivelse
            status = lastVersjon.status.name
            statusKategori = lastVersjon.statuskategori().name
            journalpostId = lastVersjon.journalpostId
            journalpostKilde = lastVersjon.journalpostkilde
            //TODO: Bør dise oppdateres?
            tildeltEnhet = lastVersjon.tildeltEnhetsnr
            tildeltSaksbehandlerident = lastVersjon.tilordnetRessurs
            //oversendtKaEnhet = mapMottakerEnhet(oppgaveKopierOrdererByVersion)
            //oversendtKaDato = mapOversendtKaDato(oppgaveKopierOrdererByVersion)
            //avsenderSaksbehandlerident = mapAvsenderSaksbehandler(oppgaveKopierOrdererByVersion)
            //avsenderEnhet = mapAvsenderEnhet(oppgaveKopierOrdererByVersion)
        }

        return Pair(klagebehandlingRepository.findByMottakId(mottak.id)!!, mottak)
    }

    private fun mapHjemler(oppgaveKopiVersjon: OppgaveKopiVersjon) =
        hjemmelService.getHjemmelFromOppgaveKopiVersjon(oppgaveKopiVersjon)

    private fun mapOrganisasjonsnummer(ident: VersjonIdent) =
        if (ident.identType == IdentType.ORGNR) {
            ident.verdi
        } else {
            null
        }
*/
}
