package no.nav.klage.oppgave.service

import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.clients.klagefssproxy.KlageFssProxyClient
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.repositories.AnkebehandlingRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CleanupHjelpemidlerService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val ankebehandlingRepository: AnkebehandlingRepository,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val klageFssProxyClient: KlageFssProxyClient,
) {

    companion object {

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun logJournalpostsWeNeedToPatch() {
        secureLogger.debug("Attempting to generate list of affected journalposts where tema HJE is wrong.")

        var logString = ""

        logString += "-------------- Klager ----------------\n"

        klagebehandlingRepository.findByYtelse(Ytelse.HJE_HJE).forEach { klagebehandling ->
            try {
                logString += logForBehandling(klagebehandling)
            } catch (e: Exception) {
                secureLogger.warn(
                    "Exception when logging list of affected journalposts where tema HJE is wrong. BehandlingId: ${klagebehandling.id}",
                    e
                )
            }
        }

        logString += "\n\n--------------- Anker -----------------\n"

        ankebehandlingRepository.findByYtelse(Ytelse.HJE_HJE).forEach { ankebehandling ->
            try {
                logString += logForBehandling(ankebehandling)
            } catch (e: Exception) {
                secureLogger.warn(
                    "Exception when logging list of affected journalposts where tema HJE is wrong. BehandlingId: ${ankebehandling.id}",
                    e
                )
            }
        }

        secureLogger.debug(logString)
    }

    private fun logForBehandling(behandling: Behandling): String {
        var logString = ""
        val sak = klageFssProxyClient.getAnySak(behandling.kildeReferanse)
        if (sak.tema != Ytelse.HJE_HJE.toTema().navn) {
            logString += if (behandling.currentDelbehandling().avsluttetAvSaksbehandler != null) {
                "Behandling finished: ${behandling.currentDelbehandling().avsluttetAvSaksbehandler}, kilderef: ${behandling.kildeReferanse}, temaToSet: ${sak.tema}\n"
            } else {
                "Behandling not finished. Kilderef: ${behandling.kildeReferanse}, temaToSet: ${sak.tema}\n"
            }

            val documentCandidates = dokumentUnderArbeidRepository.findByBehandlingId(behandling.id)
                .filter { it.parentId == null }
            logString += if (documentCandidates.isEmpty()) {
                "No documents yet\n"
            } else {
                "Documents with wrong tema:\n"
            }
            documentCandidates.forEach { documentWithWrongTema ->
                logString += "journalpostIdList: " + documentWithWrongTema.journalposter.joinToString { it.journalpostId } + "\n"
            }

            logString += "\n\n"
        }
        return logString
    }

}