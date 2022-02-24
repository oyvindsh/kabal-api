package no.nav.klage.oppgave.service.distribusjon

import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.VedtakService
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
    private val behandlingAvslutningService: BehandlingAvslutningService,
    private val kabalDocumentGateway: KabalDocumentGateway,
    private val vedtakService: VedtakService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        const val SYSTEMBRUKER = "SYSTEMBRUKER"
    }

    @Transactional(propagation = Propagation.NEVER)
    fun distribuerKlagebehandling(behandlingId: UUID) {
        try {
            val behandling =
                klagebehandlingService.getKlagebehandlingForUpdateBySystembruker(behandlingId)

            //if old way of handling documents
            if (behandling.currentDelbehandling().dokumentEnhetId != null) {
                logger.debug("Distribuerer dokument med dokumentEnhetId ${behandling.currentDelbehandling().dokumentEnhetId!!} for klagebehandling ${behandling.id}")
                try {
                    val hovedadressatJournalpostId = kabalDocumentGateway.fullfoerDokumentEnhet(behandling.currentDelbehandling().dokumentEnhetId!!)

                    vedtakService.addHovedadressatJournalpostId(
                        klagebehandlingId = behandlingId,
                        utfoerendeSaksbehandlerIdent = SYSTEMBRUKER,
                        journalpostId = hovedadressatJournalpostId
                    )

                    logger.debug("Distribuerte dokument med dokumentEnhetId ${behandling.currentDelbehandling().dokumentEnhetId!!} for klagebehandling ${behandling.id}")
                    avsluttKlagebehandling(behandling.id)
                } catch (e: Exception) {
                    logger.error(
                        "Fikk ikke distribuert dokument med dokumentEnhetId ${behandling.currentDelbehandling().dokumentEnhetId!!} for klagebehandling ${behandling.id}",
                        e
                    )
                }
            }

            //TODO new way


        } catch (e: Exception) {
            logger.error("Feilet under distribuering av klagebehandling $behandlingId", e)
        }
    }

    private fun avsluttKlagebehandling(klagebehandlingId: UUID) {
        logger.debug("Alle vedtak i klagebehandling $klagebehandlingId er ferdig distribuert, så vi markerer klagebehandlingen som avsluttet")
        behandlingAvslutningService.avsluttBehandling(klagebehandlingId)
    }
}




