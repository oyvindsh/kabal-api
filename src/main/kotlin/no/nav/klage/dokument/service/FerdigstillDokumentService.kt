package no.nav.klage.dokument.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FerdigstillDokumentService(
    private val dokumentUnderArbeidService: DokumentUnderArbeidService,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Scheduled(fixedDelay = 20000, initialDelay = 30000)
    @SchedulerLock(name = "ferdigstillDokumenter")
    fun ferdigstillHovedDokumenter() {
        val hovedDokumenter =
            dokumentUnderArbeidRepository.findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull()
        for (it in hovedDokumenter) {
            try {
                if (it.dokumentEnhetId == null) {
                    logger.debug("ferdigstillHovedDokumenter: no dokumentEnhetId. Creating for behandling ${it.behandlingId}")
                    dokumentUnderArbeidService.opprettDokumentEnhet(it.id)
                    logger.debug("ferdigstillHovedDokumenter: Done creating dokumentEnhetId (${it.dokumentEnhetId}). Behandling ${it.behandlingId}")
                }
                logger.debug("ferdigstillHovedDokumenter: Has dokumentEnhetId ${it.dokumentEnhetId}. Will try to ferdigstillDokumentEnhet. Behandling ${it.behandlingId}")
                dokumentUnderArbeidService.ferdigstillDokumentEnhet(it.id)
            } catch (e: Exception) {
                logger.error("Could not 'ferdigstillHovedDokumenter' with dokumentEnhetId: ${it.dokumentEnhetId}. See secure logs.")
                secureLogger.error(
                    "Could not 'ferdigstillHovedDokumenter' with dokumentEnhetId: ${it.dokumentEnhetId}",
                    e
                )
            }
        }
    }
}