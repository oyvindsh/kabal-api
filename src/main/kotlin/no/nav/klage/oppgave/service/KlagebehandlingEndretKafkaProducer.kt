package no.nav.klage.oppgave.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.service.mapper.KlagebehandlingSkjemaV1
import no.nav.klage.oppgave.service.mapper.mapToSkjemaV1
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KlagebehandlingEndretKafkaProducer(
    private val aivenKafkaTemplate: KafkaTemplate<String, String>,
) {
    @Value("\${KLAGE_ENDRET_TOPIC}")
    lateinit var topic: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    fun sendKlageEndret(klagebehandling: Klagebehandling) {
        logger.debug("Sending to Kafka topic: {}", topic)
        runCatching {
            val result = aivenKafkaTemplate.send(
                topic,
                klagebehandling.id.toString(),
                klagebehandling.mapToSkjemaV1().toJson()
            ).get()
            logger.info("Klage endret sent to Kafka")
            secureLogger.debug("Klage endret for klagebehandling ${klagebehandling.id} sent to kafka ($result)")
        }.onFailure {
            val errorMessage =
                "Could not send klage endret to Kafka. Need to resend klagebehandling ${klagebehandling.id} manually. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send klage endret to Kafka", it)
        }
    }

    fun KlagebehandlingSkjemaV1.toJson(): String = objectMapper.writeValueAsString(this)
}
