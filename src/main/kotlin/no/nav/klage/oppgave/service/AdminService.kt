package no.nav.klage.oppgave.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class AdminService(private val indexService: IndexService) {

    companion object {
        private const val TEN_SECONDS = 10000L
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    fun syncEsWithDb() {
        indexService.reindexAllKlagebehandlinger()
        Thread.sleep(TEN_SECONDS)
        indexService.findAndLogOldKlagebehandlinger()
    }

    fun deleteAllInES() {
        indexService.deleteAllKlagebehandlinger()
    }

    fun getAndLogOldDocuments(): Pair<Long, Long> =
        indexService.findAndLogOldKlagebehandlinger()

}