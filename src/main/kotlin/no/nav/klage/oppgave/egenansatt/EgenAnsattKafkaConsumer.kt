package no.nav.klage.oppgave.egenansatt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.oppgave.config.PartitionFinder
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.stereotype.Component


@Component
class EgenAnsattKafkaConsumer(private val egenAnsattService: EgenAnsattService, private val finder: PartitionFinder) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    @KafkaListener(
        topicPartitions = [TopicPartition(
            topic = "\${EGENANSATT_KAFKA_TOPIC}",
            partitions = ["#{@finder.partitions('\${EGENANSATT_KAFKA_TOPIC}')}"],
            partitionOffsets = [PartitionOffset(partition = "*", initialOffset = "0")]
        )]
    )
    fun listen(egenAnsattRecord: ConsumerRecord<String, String>) {
        runCatching {
            logger.debug("Reading offset ${egenAnsattRecord.offset()} from partition ${egenAnsattRecord.partition()} on kafka topic ${egenAnsattRecord.topic()}")
            val foedselsnr = egenAnsattRecord.key()
            val egenAnsatt = egenAnsattRecord.value().toEgenAnsatt()
            egenAnsattService.oppdaterEgenAnsatt(foedselsnr, egenAnsatt)
        }.onFailure {
            secureLogger.error("Failed to process egenansatt record", it)
            throw RuntimeException("Could not process egenansatt record. See more details in secure log.")
        }
    }

    private fun String.toEgenAnsatt() = mapper.readValue(this, EgenAnsatt::class.java)
}
