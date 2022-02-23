package no.nav.klage.dokument.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.MediaType
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
        private val standardMediaTypeInGCS = MediaType.valueOf("application/pdf")
    }

    @Scheduled(fixedDelay = 240000, initialDelay = 240000)
    @SchedulerLock(name = "ferdigstillDokumenter")
    fun ferdigstillHovedDokumenter() {
        val hovedDokumenter =
            dokumentUnderArbeidRepository.findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull()
        for (it in hovedDokumenter) {
            try {
                if (it.dokumentEnhetId == null) {
                    dokumentUnderArbeidService.opprettDokumentEnhet(it.id)
                }
                dokumentUnderArbeidService.ferdigstillDokumentEnhet(it.id)
            } catch (e: Exception) {
                logger.error("could not ferdigstillHovedDokumenter with dokumentEnhetId: ${it.dokumentEnhetId}")
            }
        }
    }
}