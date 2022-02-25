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
        val hovedDokumenterIkkeFerdigstilte =
            dokumentUnderArbeidRepository.findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull()
        for (it in hovedDokumenterIkkeFerdigstilte) {
            try {
                if (it.dokumentEnhetId == null) {
                    dokumentUnderArbeidService.opprettDokumentEnhet(it.id)
                }
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