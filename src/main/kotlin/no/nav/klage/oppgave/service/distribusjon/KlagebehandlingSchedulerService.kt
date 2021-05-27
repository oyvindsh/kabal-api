package no.nav.klage.oppgave.service.distribusjon

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.oppgave.service.KlagebehandlingService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class KlagebehandlingSchedulerService(
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingDistribusjonService: KlagebehandlingDistribusjonService
) {

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    @SchedulerLock(name = "distribuerVedtak")
    fun distribuerVedtak() {

        val klagebehandlinger: List<UUID> = klagebehandlingService.findKlagebehandlingForDistribusjon()

        klagebehandlinger.forEach { klagebehandling ->
            klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandling)
        }
    }
}