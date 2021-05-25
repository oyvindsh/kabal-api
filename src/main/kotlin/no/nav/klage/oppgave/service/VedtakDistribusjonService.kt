package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.dokdistfordeling.DokDistFordelingClient
import no.nav.klage.oppgave.clients.joark.JoarkClient
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setDokdistReferanseInVedtaksmottaker
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setVedtakFerdigDistribuert
import no.nav.klage.oppgave.domain.kodeverk.Rolle
import no.nav.klage.oppgave.repositories.KafkaVedtakEventRepository
import no.nav.klage.oppgave.util.AttachmentValidator
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class VedtakDistribusjonService(
    private val klagebehandlingService: KlagebehandlingService,
    private val vedtakKafkaProducer: VedtakKafkaProducer,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val attachmentValidator: AttachmentValidator,
    private val joarkClient: JoarkClient,
    private val dokumentService: DokumentService,
    private val kafkaVedtakEventRepository: KafkaVedtakEventRepository,
    private val dokDistFordelingClient: DokDistFordelingClient,
    private val safClient: SafGraphQlClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        const val SYSTEMBRUKER = "SYSTEMBRUKER" //TODO ??
    }

    @Transactional(propagation = Propagation.NESTED)
    fun distribuerJournalpostTilMottaker(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak,
        mottaker: BrevMottaker,
        utfoerendeSaksbehandlerIdent: String,
        journalfoerendeEnhet: String
    ): Vedtak? {
        return try {
            val dokdistReferanse: UUID =
                dokDistFordelingClient.distribuerJournalpost(vedtak.journalpostId!!).bestillingsId
            setDokdistReferanse(klagebehandling, vedtak.id, mottaker.id, dokdistReferanse, utfoerendeSaksbehandlerIdent)
        } catch (e: Exception) {
            logger.warn("Kunne ikke ferdigstille journalpost ${vedtak.journalpostId}")
            null
        }
    }

    fun setDokdistReferanse(
        klagebehandling: Klagebehandling,
        vedtakId: UUID,
        mottakerId: UUID,
        dokdistReferanse: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Vedtak {
        val event = klagebehandling.setDokdistReferanseInVedtaksmottaker(
            vedtakId,
            mottakerId,
            dokdistReferanse,
            utfoerendeSaksbehandlerIdent
        )
        applicationEventPublisher.publishEvent(event)
        return klagebehandling.getVedtak(vedtakId)
    }

    @Transactional(propagation = Propagation.NESTED)
    fun lagBrevmottakere(klagebehandling: Klagebehandling, vedtak: Vedtak) {
        val klager = klagebehandling.klager
        if (klager.prosessfullmektig != null) {
            leggTilProsessfullmektigSomBrevmottaker(vedtak, klager.prosessfullmektig)
            if (klager.prosessfullmektig.skalPartenMottaKopi) {
                leggTilKlagerSomBrevmottaker(vedtak, klager, false)
            }
        } else {
            leggTilKlagerSomBrevmottaker(vedtak, klager, true)
        }
        val sakenGjelder = klagebehandling.sakenGjelder
        if (sakenGjelder.partId != klager.partId) {
            leggTilSakenGjelderSomBrevmottaker(vedtak, sakenGjelder)
        }
    }

    private fun leggTilSakenGjelderSomBrevmottaker(vedtak: Vedtak, sakenGjelder: SakenGjelder) {
        vedtak.brevmottakere.add(
            BrevMottaker(
                partId = sakenGjelder.partId,
                rolle = Rolle.SAKEN_GJELDER,
                journalpostId = null
            )
        )
    }

    private fun leggTilKlagerSomBrevmottaker(vedtak: Vedtak, klager: Klager, klagerErHovedmottaker: Boolean) {
        vedtak.brevmottakere.add(
            BrevMottaker(
                partId = klager.partId,
                rolle = Rolle.KLAGER,
                journalpostId = if (klagerErHovedmottaker) vedtak.journalpostId else null
            )
        )
    }

    private fun leggTilProsessfullmektigSomBrevmottaker(vedtak: Vedtak, prosessfullmektig: Prosessfullmektig) {
        vedtak.brevmottakere.add(
            BrevMottaker(
                partId = prosessfullmektig.partId,
                rolle = Rolle.PROSESSFULLMEKTIG,
                journalpostId = vedtak.journalpostId
            )
        )
    }

    @Transactional(propagation = Propagation.NESTED)
    fun lagKopiAvJournalpostForMottaker(vedtak: Vedtak, brevMottaker: BrevMottaker) {
        //TODO: Kod opp dette
        throw IllegalStateException("Dette har vi ikke kodet opp ennå, K9 støtter bare en mottaker")
    }

    @Transactional(propagation = Propagation.NESTED)
    fun markerVedtakSomFerdigDistribuert(klagebehandling: Klagebehandling, vedtak: Vedtak) {
        val event = klagebehandling.setVedtakFerdigDistribuert(vedtak.id, SYSTEMBRUKER)
        applicationEventPublisher.publishEvent(event)
    }
}
