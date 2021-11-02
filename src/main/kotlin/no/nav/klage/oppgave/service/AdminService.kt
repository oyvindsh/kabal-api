package no.nav.klage.oppgave.service

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.oppgave.domain.kafka.EventType
import no.nav.klage.oppgave.domain.kafka.UtsendingStatus.*
import no.nav.klage.oppgave.util.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class AdminService(private val indexService: IndexService, private val kafkaDispatcher: KafkaDispatcher) {

    companion object {
        private const val TWO_SECONDS = 2000L

        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun syncEsWithDb() {
        indexService.reindexAllKlagebehandlinger()
        Thread.sleep(TWO_SECONDS)
        indexService.findAndLogOutOfSyncKlagebehandlinger()
    }

    fun deleteAllInES() {
        indexService.deleteAllKlagebehandlinger()
        Thread.sleep(TWO_SECONDS)
    }

    fun recreateEsIndex() {
        indexService.recreateIndex()
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    @SchedulerLock(name = "findAndLogOutOfSyncKlagebehandlinger")
    fun findAndLogOutOfSyncKlagebehandlinger() =
        indexService.findAndLogOutOfSyncKlagebehandlinger()

    fun resendToDVH() {
        logger.debug("Attempting to resend all events to DVH")
        kafkaDispatcher.dispatchEventsToKafka(
            EventType.STATS_DVH,
            listOf(IKKE_SENDT, FEILET, SENDT)
        )
    }

}