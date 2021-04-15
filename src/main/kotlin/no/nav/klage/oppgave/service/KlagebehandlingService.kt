package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
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
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.exceptions.KlagebehandlingNotFoundException
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
    private val hjemmelService: HjemmelService,
    private val vedtakKafkaProducer: VedtakKafkaProducer,
    private val dokumentService: DokumentService
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

    @Transactional(readOnly = true)
    fun getKlagebehandling(klagebehandlingId: UUID): Klagebehandling =
        klagebehandlingRepository.findById(klagebehandlingId)
            .orElseThrow { KlagebehandlingNotFoundException("Klagebehandling med id $klagebehandlingId ikke funnet") }
            .also { checkTilgang(it) }

    fun getKlagebehandlingForUpdate(klagebehandlingId: UUID, klagebehandlingVersjon: Long?): Klagebehandling =
        klagebehandlingRepository.getOne(klagebehandlingId)
            .also { checkTilgang(it) }
            .also { it.checkOptimisticLocking(klagebehandlingVersjon) }

    fun assignKlagebehandling(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        tildeltSaksbehandlerIdent: String?,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setTildeltSaksbehandlerident(tildeltSaksbehandlerIdent, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setSakstype(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        sakstype: Sakstype,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setSakstype(sakstype, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setTema(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        tema: Tema,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setTema(tema, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setInnsendt(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        innsendt: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setInnsendt(innsendt, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setMottattFoersteinstans(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        mottattFoersteinstans: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setMottattFoersteinstans(mottattFoersteinstans, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setMottattKlageinstans(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        mottattKlageinstans: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setMottattKlageinstans(mottattKlageinstans, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setFrist(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        frist: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event =
            klagebehandling.setFrist(frist, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setAvsenderSaksbehandleridentFoersteinstans(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        saksbehandlerIdent: String,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
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
        klagebehandlingVersjon: Long?,
        enhet: String,
        utfoerendeSaksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
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
        klagebehandlingVersjon: Long?,
        grunn: Grunn?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event = klagebehandling.setKvalitetsvurderingGrunn(grunn, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingEoes(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        eoes: Eoes?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event = klagebehandling.setKvalitetsvurderingEoes(eoes, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingRaadfoertMedLege(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        raadfoertMedLege: RaadfoertMedLege?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event = klagebehandling.setKvalitetsvurderingRaadfoertMedLege(raadfoertMedLege, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingInternVurdering(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        internVurdering: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event = klagebehandling.setKvalitetsvurderingInternvurdering(internVurdering, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingSendTilbakemelding(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        sendTilbakemelding: Boolean?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event = klagebehandling.setKvalitetsvurderingSendTilbakemelding(sendTilbakemelding, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun setKvalitetsvurderingTilbakemelding(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        tilbakemelding: String?,
        saksbehandlerIdent: String
    ): Klagebehandling {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
        val event = klagebehandling.setKvalitetsvurderingTilbakemelding(tilbakemelding, saksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return klagebehandling
    }

    fun createKlagebehandlingFromMottak(mottak: Mottak) {
        if (klagebehandlingRepository.findByMottakId(mottak.id) != null) {
            logger.error("We already have a klagebehandling for mottak ${mottak.id}. This is not supposed to happen.")
            throw RuntimeException("We already have a klagebehandling for mottak ${mottak.id}")
        }

        val klagebehandling = klagebehandlingRepository.save(
            Klagebehandling(
                foedselsnummer = mottak.klagerPartId.value, // TODO Her må vi fikse
                tema = mottak.tema,
                sakstype = mottak.sakstype,
                referanseId = mottak.internReferanse,
                innsendt = mottak.innsendtDato,
                mottattFoersteinstans = mottak.mottattNavDato,
                avsenderEnhetFoersteinstans = mottak.avsenderEnhet,
                avsenderSaksbehandleridentFoersteinstans = mottak.avsenderSaksbehandlerident,
                mottattKlageinstans = mottak.oversendtKaDato,
                startet = null,
                avsluttet = null,
                frist = mottak.fristFraFoersteinstans,
                tildeltSaksbehandlerident = null,
                tildeltEnhet = mottak.oversendtKaEnhet,
                mottakId = mottak.id,
                vedtak = mutableSetOf(),
                kvalitetsvurdering = null,
                hjemler = mottak.hjemler().map { hjemmelService.generateHjemmelFromText(it) }.toMutableSet(),
                saksdokumenter = createSaksdokumenter(mottak),
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

    private fun createSaksdokumenter(mottak: Mottak): MutableSet<Saksdokument> {
        val saksdokumenter: MutableSet<Saksdokument> = mutableSetOf()
        mottak.mottakDokument.forEach {
            //TODO: Mangler å få med MottakDokument.type over i Saksdokument!
            saksdokumenter.addAll(createSaksdokument(it.journalpostId))
        }
        return saksdokumenter
    }

    private fun createSaksdokument(journalpostId: String) =
        dokumentService.fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId)
            .map { Saksdokument(journalpostId = journalpostId, dokumentInfoId = it) }

    private fun mapSakstype(behandlingstype: String): Sakstype = Sakstype.of(behandlingstype)

    private fun mapTema(tema: String): Tema = Tema.of(tema)

    fun addDokument(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
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
        klagebehandlingVersjon: Long?,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        val klagebehandling = getKlagebehandlingForUpdate(klagebehandlingId, klagebehandlingVersjon)
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

    fun fullfoerVedtak(klagebehandlingId: UUID, vedtakId: UUID) {
        val klage = klagebehandlingRepository.findById(klagebehandlingId).orElseThrow()
        val vedtak = klage.vedtak.find { it.id == vedtakId }
        require(vedtak != null) { "Fant ikke vedtak på klage" }
        val vedtakFattet = KlagevedtakFattet(
            id = klage.referanseId ?: "UKJENT", // TODO: Riktig?
            utfall = vedtak.utfall,
            vedtaksbrevReferanse = "TODO"
        )

        vedtakKafkaProducer.sendVedtak(vedtakFattet)
    }

    fun fetchDokumentlisteForKlagebehandling(
        klagebehandlingId: UUID,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        return dokumentService.fetchDokumentlisteForKlagebehandling(klagebehandling, pageSize, previousPageRef)
    }

    fun fetchJournalpostIderConnectedToKlagebehandling(klagebehandlingId: UUID): List<String> =
        getKlagebehandling(klagebehandlingId).saksdokumenter.map { it.journalpostId }

    fun fetchJournalposterConnectedToKlagebehandling(klagebehandlingId: UUID): DokumenterResponse {
        val klagebehandling = getKlagebehandling(klagebehandlingId)
        return dokumentService.fetchJournalposterConnectedToKlagebehandling(klagebehandling)
    }

    fun connectDokumentToKlagebehandling(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        dokumentService.validateJournalpostExists(journalpostId)
        addDokument(
            klagebehandlingId,
            klagebehandlingVersjon,
            journalpostId,
            dokumentInfoId,
            saksbehandlerIdent
        )
    }

    fun disconnectDokumentFromKlagebehandling(
        klagebehandlingId: UUID,
        klagebehandlingVersjon: Long?,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        removeDokument(
            klagebehandlingId,
            klagebehandlingVersjon,
            journalpostId,
            dokumentInfoId,
            saksbehandlerIdent
        )
    }
}
