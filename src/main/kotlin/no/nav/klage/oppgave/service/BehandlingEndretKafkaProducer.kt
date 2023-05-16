package no.nav.klage.oppgave.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandling
import no.nav.klage.oppgave.domain.klage.Ankebehandling
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.service.mapper.BehandlingSkjemaV2
import no.nav.klage.oppgave.service.mapper.mapToSkjemaV2
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class BehandlingEndretKafkaProducer(
    private val aivenKafkaTemplate: KafkaTemplate<String, String>,
) {
    @Value("\${BEHANDLING_ENDRET_TOPIC_V2}")
    lateinit var topicV2: String

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    }

    fun sendKlageEndretV2(klagebehandling: Klagebehandling) {
        logger.debug("Sending to Kafka topic: {}", topicV2)

        val json = klagebehandling.mapToSkjemaV2().toJson()
        secureLogger.debug("Sending to Kafka topic: {}, value: {}", topicV2, json)

        runCatching {
            val result = aivenKafkaTemplate.send(
                topicV2,
                klagebehandling.id.toString(),
                json
            ).get()
            logger.info("Klage endret sent to Kafka")
            secureLogger.debug("Klage endret for klagebehandling ${klagebehandling.id} sent to kafka ($result)")
        }.onFailure {
            val errorMessage =
                "Could not send klage endret to Kafka. Need to resend klagebehandling ${klagebehandling.id} manually. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send klagebehandling ${klagebehandling.id} endret to Kafka", it)
        }
    }

    fun sendAnkeEndretV2(ankebehandling: Ankebehandling) {
        logger.debug("Sending to Kafka topic: {}", topicV2)
        runCatching {
            val result = aivenKafkaTemplate.send(
                topicV2,
                ankebehandling.id.toString(),
                ankebehandling.mapToSkjemaV2().toJson()
            ).get()
            logger.info("Anke endret sent to Kafka")
            secureLogger.debug("Anke endret for ankebehandling ${ankebehandling.id} sent to kafka ($result)")
        }.onFailure {
            val errorMessage =
                "Could not send anke endret to Kafka. Need to resend ankebehandling ${ankebehandling.id} manually. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error("Could not send ankebehandling ${ankebehandling.id} endret to Kafka", it)
        }
    }

    fun sendAnkeITrygderettenEndretV2(ankeITrygderettenbehandling: AnkeITrygderettenbehandling) {
        logger.debug("Sending to Kafka topic: {}", topicV2)
        runCatching {
            val result = aivenKafkaTemplate.send(
                topicV2,
                ankeITrygderettenbehandling.id.toString(),
                ankeITrygderettenbehandling.mapToSkjemaV2().toJson()
            ).get()
            logger.info("Anke i trygderetten endret sent to Kafka")
            secureLogger.debug("Anke i trygderetten endret for ankebehandling ${ankeITrygderettenbehandling.id} sent to kafka ($result)")
        }.onFailure {
            val errorMessage =
                "Could not send anke i trygderetten endret to Kafka. Need to resend ankeITrygderettenbehandling ${ankeITrygderettenbehandling.id} manually. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error(
                "Could not send ankeITrygderettenbehandling ${ankeITrygderettenbehandling.id} endret to Kafka",
                it
            )
        }
    }

    fun sendBehandlingDeleted(behandlingId: UUID) {
        logger.debug("Sending null message (for delete) to Kafka topic: {}", topicV2)
        runCatching {
            val result = aivenKafkaTemplate.send(topicV2, behandlingId.toString(), null).get()
            logger.info("Behandling deleted sent to Kafka")
            secureLogger.debug("Behandling deleted sent to Kafka ($result)")
        }.onFailure {
            val errorMessage =
                "Could not send klage deleted to Kafka. Need to resend behandling $behandlingId manually. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error(errorMessage, it)
        }
    }

    fun BehandlingSkjemaV2.toJson(): String = objectMapper.writeValueAsString(this)
}
