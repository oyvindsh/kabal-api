package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.VedtakFullfoerInput
import no.nav.klage.oppgave.clients.joark.JoarkClient
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.domain.klage.KafkaVedtakEvent
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setFinalizedIdInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setGrunnInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setHjemlerInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setJournalpostIdInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setUtfallInVedtak
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatus
import no.nav.klage.oppgave.exceptions.JournalpostFinalizationException
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.exceptions.VedtakFinalizedException
import no.nav.klage.oppgave.exceptions.VedtakNotFoundException
import no.nav.klage.oppgave.repositories.KafkaVedtakEventRepository
import no.nav.klage.oppgave.util.AttachmentValidator
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
@Transactional
class VedtakService(
    private val klagebehandlingService: KlagebehandlingService,
    private val vedtakKafkaProducer: VedtakKafkaProducer,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val attachmentValidator: AttachmentValidator,
    private val joarkClient: JoarkClient,
    private val dokumentService: DokumentService,
    private val kafkaVedtakEventRepository: KafkaVedtakEventRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional(readOnly = true)
    fun getVedtak(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setUtfall(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfall: Utfall,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setUtfallInVedtak(vedtakId, utfall, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setGrunn(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        grunn: Grunn?,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setGrunnInVedtak(vedtakId, grunn, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setHjemler(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        hjemler: Set<Hjemmel>,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setHjemlerInVedtak(vedtakId, hjemler, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setJournalpostId(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        journalpostId: String,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setJournalpostIdInVedtak(vedtakId, journalpostId, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun setFinalized(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event =
            klagebehandling.setFinalizedIdInVedtak(vedtakId, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return getVedtakFromKlagebehandling(klagebehandling, vedtakId)
    }

    fun addVedlegg(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        vedlegg: MultipartFile,
        utfoerendeSaksbehandlerIdent: String,
        journalfoerendeEnhet: String
    ): Vedtak {
        val vedtak = getVedtakFromKlagebehandling(klagebehandling, vedtakId)
        if (vedtak.finalized != null) throw VedtakFinalizedException("Vedtak med id $vedtakId er ferdigstilt")
        attachmentValidator.validateAttachment(vedlegg)
        if (vedtak.journalpostId != null) {
            joarkClient.cancelJournalpost(vedtak.journalpostId!!, journalfoerendeEnhet)
        }

        return setJournalpostId(
            klagebehandling,
            vedtakId,
            joarkClient.createJournalpost(klagebehandling, vedlegg, journalfoerendeEnhet),
            utfoerendeSaksbehandlerIdent
        )
    }

    fun getVedlegg(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): ArkivertDokument? {
        val vedtak = getVedtakFromKlagebehandling(klagebehandling, vedtakId)
        return if (vedtak.journalpostId != null) {
            dokumentService.getMainDokument(vedtak.journalpostId!!)
        } else {
            null
        }
    }

    fun finalizeVedtak(
        klagebehandlingId: UUID,
        vedtakId: UUID,
        input: VedtakFullfoerInput,
        innloggetIdent: String
    ) {
        val klage = klagebehandlingService.getKlagebehandlingForUpdate(
            klagebehandlingId,
            input.klagebehandlingVersjon
        )
        val vedtak = getVedtakFromKlagebehandling(klage, vedtakId)
        if (vedtak.finalized != null) throw VedtakFinalizedException("Vedtak med id $vedtakId er allerede ferdigstilt")
        if (vedtak.journalpostId == null) throw JournalpostNotFoundException("Vedtak med id $vedtakId er ikke journalført")
        require(vedtak.utfall != null) { "Utfall på vedtak må være satt" }

        finalizeJournalpost(
            klage,
            vedtak,
            innloggetIdent,
            input.journalfoerendeEnhet
        )

        addVedtakToStore(klage, vedtak)
    }

    private fun finalizeJournalpost(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak,
        utfoerendeSaksbehandlerIdent: String,
        journalfoerendeEnhet: String
    ): Vedtak? {
        return try {
            joarkClient.finalizeJournalpost(vedtak.journalpostId!!, journalfoerendeEnhet)
            setFinalized(klagebehandling, vedtak.id, utfoerendeSaksbehandlerIdent)
        } catch (e: Exception) {
            logger.warn("Kunne ikke ferdigstille journalpost ${vedtak.journalpostId}")
            throw JournalpostFinalizationException("Klarte ikke å journalføre vedtak")
        }
    }

    // TODO Legg til brevutsending i eget steg?

    private fun addVedtakToStore(klage: Klagebehandling, vedtak: Vedtak) {
        kafkaVedtakEventRepository.save(
            KafkaVedtakEvent(
                kildeReferanse = klage.kildeReferanse ?: "UKJENT",
                kilde = klage.kildesystem.name,
                utfall = vedtak.utfall!!,
                vedtaksbrevReferanse = vedtak.journalpostId,
                kabalReferanse = vedtak.id.toString(),
                status = UtsendingStatus.IKKE_SENDT
            )
        )
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    private fun dispatchUnsendtVedtakToKafka() {
        kafkaVedtakEventRepository.getAllByStatusIsNotLikeSENDT().forEach { event ->
            runCatching {
                vedtakKafkaProducer.sendVedtak(
                    KlagevedtakFattet(
                        kildeReferanse = event.kildeReferanse,
                        kilde = event.kilde,
                        utfall = event.utfall,
                        vedtaksbrevReferanse = event.vedtaksbrevReferanse,
                        kabalReferanse = event.kabalReferanse
                    )
                )
            }.onFailure {
                event.status = UtsendingStatus.FEILET
                event.melding = it.message
                logger.error("Send event ${event.id} to kafka failed, see secure log for details")
                secureLogger.error("Send event ${event.id} to kafka failed. Object: $event")
            }.onSuccess {
                event.status = UtsendingStatus.SENDT
                event.melding = null
                logger.info("Event ${event.id} dispatched to kafka")
                secureLogger.info("Event $event dispatched to kafka")
            }
        }
    }

    private fun getVedtakFromKlagebehandling(klagebehandling: Klagebehandling, vedtakId: UUID): Vedtak {
        return klagebehandling.vedtak.firstOrNull {
            it.id == vedtakId
        } ?: throw VedtakNotFoundException("Vedtak med id $vedtakId ikke funnet")
    }


}
