package no.nav.klage.oppgave.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class VedtakKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>
) {
    @Value("\${VEDTAK_FATTET_TOPIC}")
    lateinit var topic: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    fun sendVedtak(vedtak: KlagevedtakFattet) {
        logger.debug("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nVedtak: {}", topic, vedtak)
        runCatching {
            val result = kafkaTemplate.send(topic, vedtak.toJson()).get()
            logger.info("Vedtak sent to Kafka.")
            secureLogger.debug("Vedtak $vedtak sent to kafka ($result)")
        }.onFailure {
            val errorMessage = "Could not send vedtak to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send vedtak to Kafka", it)
            throw RuntimeException(errorMessage)
        }
    }

    private fun Any.toJson(): String = objectMapper.writeValueAsString(this)
}
