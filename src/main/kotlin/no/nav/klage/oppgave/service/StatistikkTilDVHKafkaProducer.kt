package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*

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

    }

    fun sendStatistikkTilDVH(klagebehandlingId: UUID, json: String) {
        logger.debug("Sending to Kafka topic: {}", topic)
        secureLogger.debug("Sending to Kafka topic: {}\npayload: {}", topic, json)
        runCatching {
            aivenKafkaTemplate.send(topic, klagebehandlingId.toString(), json).get()
            logger.debug("Klagestatistikk til DVH sent to Kafka.")
        }.onFailure {
            val errorMessage =
                "Could not send Klagestatistikk til DVH to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send Klagestatistikk til DVH to Kafka", it)
        }
    }
}
