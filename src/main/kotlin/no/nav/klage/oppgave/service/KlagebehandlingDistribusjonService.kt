package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.BrevMottaker
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Vedtak
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
            klagebehandling.vedtak.forEach { vedtak ->
                if (vedtak.brevmottakere.isEmpty()) {
                    vedtakDistribusjonService.lagBrevmottakere(klagebehandling, vedtak)
                }
                vedtak.brevmottakere.forEach { brevMottaker ->
                    if (brevMottaker.journalpostId != vedtak.journalpostId) {
                        vedtakDistribusjonService.lagKopiAvJournalpostForMottaker(vedtak, brevMottaker)
                    }
                    distribuerVedtakTilMottaker(klagebehandling, vedtak, brevMottaker)
                }
            }
            klagebehandlingAvslutningService.avsluttKlagebehandling(klagebehandling)
        } catch (e: Exception) {
            logger.error("Feilet under distribuering av klagebehandling $klagebehandlingId", e)
        }
    }

    private fun distribuerVedtakTilMottaker(
        klagebehandling: Klagebehandling,
        vedtak: Vedtak,
        brevMottaker: BrevMottaker,
    ) {
        if (brevMottaker.dokdistReferanse != null) {
            vedtakDistribusjonService.distribuerJournalpostTilMottaker(
                klagebehandling,
                vedtak,
                brevMottaker,
                VedtakDistribusjonService.SYSTEMBRUKER,
                klagebehandling.tildeltEnhet!!
            )
        }
    }
}