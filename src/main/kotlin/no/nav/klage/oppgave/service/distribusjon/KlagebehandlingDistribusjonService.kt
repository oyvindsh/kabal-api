package no.nav.klage.oppgave.service.distribusjon

import no.nav.klage.oppgave.domain.klage.BrevMottaker
import no.nav.klage.oppgave.domain.klage.Vedtak
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KlagebehandlingDistribusjonService(
    private val klagebehandlingService: KlagebehandlingService,
    private val vedtakDistribusjonService: VedtakDistribusjonService,
    private val klagebehandlingAvslutningService: KlagebehandlingAvslutningService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun distribuerKlagebehandling(klagebehandlingId: UUID) {
        try {
            val klagebehandling = klagebehandlingService.getKlagebehandlingForUpdate(klagebehandlingId, null)
            logger.debug("1. Klagebehandlingversjon er ${klagebehandling.versjon}")
            klagebehandling.vedtak
                .filter { vedtak -> vedtak.erIkkeFerdigDistribuert() }
                .forEach { vedtak ->
                    logger.debug("Starter sjekk for om vedtak ${vedtak.id} skal distribueres")
                    if (vedtak.harIngenBrevMottakere()) {
                        logger.debug("Vedtak ${vedtak.id} har ingen brevmottakere")
                        vedtakDistribusjonService.lagBrevmottakere(klagebehandling, vedtak.id)
                    }
                    logger.debug("2. Klagebehandlingversjon er ${klagebehandling.versjon}")
                    vedtak.brevmottakere
                        .filter { brevMottaker -> brevMottaker.erIkkeDistribuertTil() }
                        .forEach { brevMottaker ->
                            logger.debug("Starter distribusjon av vedtak ${vedtak.id} til brevmottaker ${brevMottaker.id}")
                            if (brevMottaker.erIkkeHovedMottakerAv(vedtak)) {
                                vedtakDistribusjonService.lagKopiAvJournalpostForMottaker(vedtak, brevMottaker)
                            }
                            logger.debug("3. Klagebehandlingversjon er ${klagebehandling.versjon}")
                            vedtakDistribusjonService.distribuerJournalpostTilMottaker(
                                klagebehandling, vedtak, brevMottaker
                            )
                            logger.debug("4. Klagebehandlingversjon er ${klagebehandling.versjon}")
                        }
                    vedtakDistribusjonService.markerVedtakSomFerdigDistribuert(klagebehandling, vedtak)
                    logger.debug("5. Klagebehandlingversjon er ${klagebehandling.versjon}")
                }
            klagebehandlingAvslutningService.avsluttKlagebehandling(klagebehandling)
            logger.debug("6. Klagebehandlingversjon er ${klagebehandling.versjon}")
        } catch (e: Exception) {
            logger.error("Feilet under distribuering av klagebehandling $klagebehandlingId", e)
        }
    }

    private fun BrevMottaker.erIkkeDistribuertTil() = this.dokdistReferanse == null

    private fun BrevMottaker.erIkkeHovedMottakerAv(vedtak: Vedtak) = this.journalpostId != vedtak.journalpostId

    private fun Vedtak.erIkkeFerdigDistribuert() = ferdigDistribuert == null

    private fun Vedtak.harIngenBrevMottakere(): Boolean =
        brevmottakere.isEmpty()
}




