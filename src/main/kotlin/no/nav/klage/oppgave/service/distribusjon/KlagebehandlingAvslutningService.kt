package no.nav.klage.oppgave.service.distribusjon

import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.domain.klage.KafkaVedtakEvent
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KlagebehandlingAggregatFunctions.setAvsluttet
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatus
import no.nav.klage.oppgave.repositories.KafkaVedtakEventRepository
import no.nav.klage.oppgave.service.VedtakKafkaProducer
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class KlagebehandlingAvslutningService(
    private val kafkaVedtakEventRepository: KafkaVedtakEventRepository,
    private val vedtakKafkaProducer: VedtakKafkaProducer,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional(propagation = Propagation.REQUIRED)
    fun avsluttKlagebehandling(klagebehandling: Klagebehandling) {

        val vedtak = klagebehandling.vedtak.first()
        kafkaVedtakEventRepository.save(
            KafkaVedtakEvent(
                kildeReferanse = klagebehandling.kildeReferanse ?: "UKJENT",
                kilde = klagebehandling.kildesystem.name,
                utfall = vedtak.utfall!!,
                vedtaksbrevReferanse = vedtak.journalpostId,
                kabalReferanse = vedtak.id.toString(),
                status = UtsendingStatus.IKKE_SENDT
            )
        )

        klagebehandling.setAvsluttet(VedtakDistribusjonService.SYSTEMBRUKER)
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "Europe/Paris")
    @Transactional
    fun dispatchUnsendtVedtakToKafka() {
        kafkaVedtakEventRepository.getAllByStatusIsNotLike(UtsendingStatus.SENDT).forEach { event ->
            runCatching {
                vedtakKafkaProducer.sendVedtak(
                    KlagevedtakFattet(
                        kildeReferanse = event.kildeReferanse,
                        kilde = event.kilde,
                        utfall = event.utfall,
                        vedtaksbrevReferanse = event.vedtaksbrevReferanse,
                        kabalReferanse = event.kabalReferanse
                    )
                )
            }.onFailure {
                event.status = UtsendingStatus.FEILET
                event.melding = it.message
                logger.error("Send event ${event.id} to kafka failed, see secure log for details")
                secureLogger.error("Send event ${event.id} to kafka failed. Object: $event")
            }.onSuccess {
                event.status = UtsendingStatus.SENDT
                event.melding = null
            }
        }
    }
}