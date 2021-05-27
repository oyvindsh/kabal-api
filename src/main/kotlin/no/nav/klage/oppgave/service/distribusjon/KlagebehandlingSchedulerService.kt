package no.nav.klage.oppgave.service.distribusjon

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.oppgave.service.KlagebehandlingService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class KlagebehandlingSchedulerService(
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingDistribusjonService: KlagebehandlingDistribusjonService,
    private val klagebehandlingAvslutningService: KlagebehandlingAvslutningService
) {

    @Scheduled(fixedDelay = 240000, initialDelay = 240000)
    @SchedulerLock(name = "distribuerVedtak")
    fun distribuerVedtak() {

        val klagebehandlinger: List<UUID> = klagebehandlingService.findKlagebehandlingForDistribusjon()

        klagebehandlinger.forEach { klagebehandling ->
            klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandling)
        }
    }

    @Scheduled(fixedDelay = 240000, initialDelay = 300000)
    @SchedulerLock(name = "dispatchUnsendtVedtakToKafka")
    fun dispatchUnsendtVedtakToKafka() {
        klagebehandlingAvslutningService.dispatchUnsendtVedtakToKafka()
    }
}