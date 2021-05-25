package no.nav.klage.oppgave.service

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KlagebehandlingSchedulerService(
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingDistribusjonService: KlagebehandlingDistribusjonService
) {

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    @Transactional
    fun distribuerVedtak() {

        val klagebehandlinger: List<UUID> = klagebehandlingService.findKlagebehandlingForDistribusjon()

        klagebehandlinger.forEach { klagebehandling ->
            klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandling)
        }
    }
}