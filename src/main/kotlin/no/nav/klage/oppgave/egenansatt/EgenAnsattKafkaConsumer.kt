package no.nav.klage.oppgave.egenansatt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.listener.ConsumerSeekAware.ConsumerSeekCallback
import org.springframework.stereotype.Component


@Component
class EgenAnsattKafkaConsumer(private val egenAnsattService: EgenAnsattService) :
    ConsumerSeekAware {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    @KafkaListener(topics = ["\${EGENANSATT_KAFKA_TOPIC}"])
    fun listen(egenAnsattRecord: ConsumerRecord<String, String>) {
        runCatching {
            logger.debug("Reading offset ${egenAnsattRecord.offset()} from partition ${egenAnsattRecord.partition()} on kafka topic ${egenAnsattRecord.topic()}")
            val fodselsnr = egenAnsattRecord.key()
            val egenAnsatt = egenAnsattRecord.value().toEgenAnsatt()
            egenAnsattService.oppdaterEgenAnsatt(fodselsnr, egenAnsatt)
        }.onFailure {
            secureLogger.error("Failed to process egenansatt record", it)
            throw RuntimeException("Could not process egenansatt record. See more details in secure log.")
        }
    }

    private fun String.toEgenAnsatt() = mapper.readValue(this, EgenAnsatt::class.java)

    override fun onPartitionsAssigned(assignments: Map<TopicPartition?, Long?>, callback: ConsumerSeekCallback) {
        logger.info("Seeking to beginning of ${assignments.keys}")
        callback.seekToBeginning(assignments.keys)
    }

    override fun registerSeekCallback(callback: ConsumerSeekCallback) {}

    override fun onPartitionsRevoked(partitions: Collection<TopicPartition?>) {}

    override fun onIdleContainer(assignments: Map<TopicPartition?, Long?>?, callback: ConsumerSeekCallback?) {}
}
