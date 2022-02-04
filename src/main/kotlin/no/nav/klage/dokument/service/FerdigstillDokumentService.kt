package no.nav.klage.dokument.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.dokument.repositories.HovedDokumentRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FerdigstillDokumentService(
    private val dokumentService: DokumentService,
    private val hovedDokumentRepository: HovedDokumentRepository,
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
            hovedDokumentRepository.findByMarkertFerdigNotNullAndFerdigstiltNull()
        hovedDokumenter.forEach {
            if (it.dokumentEnhetId == null) {
                dokumentService.opprettDokumentEnhet(it.id)
            }
            dokumentService.ferdigstillDokumentEnhet(it.id)
        }
    }
}