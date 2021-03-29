package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class VedtakService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val vedtakKafkaProducer: VedtakKafkaProducer
) {

    fun fullfoerVedtak(vedtakId: UUID) {
        val klage = klagebehandlingRepository.findByVedtakId(vedtakId)
        require(klage != null) { "Ingen klage knyttet til vedtak" }
        val vedtak = klage.vedtak.find { it.id == vedtakId }
        require(vedtak != null) { "Fant ikke vedtak på klage" }
        val vedtakFattet = KlagevedtakFattet(
            id = klage.referanseId ?: "UKJENT", // TODO: Riktig?
            utfall = vedtak.utfall,
            vedtaksbrevReferanse = "TODO"
        )

        vedtakKafkaProducer.sendVedtak(vedtakFattet)
    }

}
