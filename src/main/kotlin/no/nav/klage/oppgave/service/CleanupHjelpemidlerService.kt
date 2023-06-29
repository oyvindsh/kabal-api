package no.nav.klage.oppgave.service

import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.clients.klagefssproxy.KlageFssProxyClient
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
                val sak = klageFssProxyClient.getSak(klagebehandling.kildeReferanse)
                if (sak.tema != Ytelse.HJE_HJE.toTema().navn) {
                    logString += "Behandling finished: ${klagebehandling.currentDelbehandling().avsluttetAvSaksbehandler}, kilderef: ${klagebehandling.kildeReferanse}, temaToSet: ${sak.tema}\n"
                    logString += "Documents with wrong tema:\n"

                    dokumentUnderArbeidRepository.findByBehandlingId(klagebehandling.id).filter { it.parentId == null }
                        .forEach { documentWithWrongTema ->
                            logString += "temaToSet: ${sak.tema}, journalpostIdList: " + documentWithWrongTema.journalposter.joinToString { it.journalpostId } + "\n"
                        }

                    logString += "\n\n"
                }
            } catch (e: Exception) {
                secureLogger.warn(
                    "Exception when logging list of affected journalposts where tema HJE is wrong. BehandlingId: ${klagebehandling.id}",
                    e
                )
            }
        }

        logString += "\n\n------------------ Anker ---------------------\n"

        ankebehandlingRepository.findByYtelse(Ytelse.HJE_HJE).forEach { ankebehandling ->
            try {
                val sak = klageFssProxyClient.getSak(ankebehandling.kildeReferanse)
                if (sak.tema != Ytelse.HJE_HJE.toTema().navn) {
                    logString += "Behandling finished: ${ankebehandling.currentDelbehandling().avsluttetAvSaksbehandler}, kilderef: ${ankebehandling.kildeReferanse}, temaToSet: ${sak.tema}\n"
                    logString += "Documents with wrong tema:\n"

                    dokumentUnderArbeidRepository.findByBehandlingId(ankebehandling.id).filter { it.parentId == null }
                        .forEach { documentWithWrongTema ->
                            logString += "kilderef: ${ankebehandling.kildeReferanse}, temaToSet: ${sak.tema}, journalpostIdList: " + documentWithWrongTema.journalposter.joinToString { it.journalpostId } + "\n"
                        }
                }
            } catch (e: Exception) {
                secureLogger.warn(
                    "Exception when logging list of affected journalposts where tema HJE is wrong. BehandlingId: ${ankebehandling.id}",
                    e
                )
            }
        }

        secureLogger.debug(logString)
    }

}