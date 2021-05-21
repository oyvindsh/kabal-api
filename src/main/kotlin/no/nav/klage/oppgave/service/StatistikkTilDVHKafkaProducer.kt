package no.nav.klage.oppgave.service

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.oppgave.domain.kafka.KlageStatistikkTilDVH
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class StatistikkTilDVHKafkaProducer(
    private val aivenKafkaTemplate: KafkaTemplate<String, String>
) {
    @Value("\${DVH_STATISTIKK_TOPIC}")
    lateinit var topic: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    fun sendStatistikkTilDVH(statistikk: KlageStatistikkTilDVH) {
        logger.debug("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\nKlageStatistikkTilDVH: {}", topic, statistikk)
        runCatching {
            aivenKafkaTemplate.send(topic, statistikk.toJson()).get()
            logger.debug("KlageStatistikkTilDVH sent to Kafka.")
        }.onFailure {
            val errorMessage = "Could not send KlageStatistikkTilDVH to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send KlageStatistikkTilDVH to Kafka", it)
        }
    }

    private fun KlageStatistikkTilDVH.toJson(): String = objectMapper.writeValueAsString(this)
}
