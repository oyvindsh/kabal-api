package no.nav.klage.oppgave.service

import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.clients.klagefssproxy.KlageFssProxyClient
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CleanupHjeService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val klageFssProxyClient: KlageFssProxyClient,
) {

    companion object {

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun logJournalpostsWeNeedToPatch() {
        logger.debug("Attempting to generate list of affected journalposts where tema is wrong.")

        var logString = ""

        klagebehandlingRepository.findByYtelse(Ytelse.HJE_HJE).forEach { klagebehandling ->
            try {
                val sak = klageFssProxyClient.getSak(klagebehandling.kildeReferanse)
                if (sak.tema != Ytelse.HJE_HJE.toTema().navn) {
                    dokumentUnderArbeidRepository.findByBehandlingId(klagebehandling.id).filter { it.parentId == null }
                        .forEach { documentWithWrongTema ->
                            logString += "temaToSet: ${sak.tema}, journalpostIdList: " + documentWithWrongTema.journalposter.joinToString { it.journalpostId } + "\n"
                        }
                }
            } catch (e: Exception) {
                logger.warn("???", e)
            }
        }

        logger.debug(logString)
    }

}