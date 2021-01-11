package no.nav.klage.oppgave.pdl

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
class PdlKafkaConsumer(private val finder: PartitionFinder) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    @KafkaListener(
        topicPartitions = [TopicPartition(
            topic = "\${PDL_PERSON_KAFKA_TOPIC}",
            partitions = ["#{@finder.partitions('\${PDL_PERSON_KAFKA_TOPIC}')}"],
            partitionOffsets = [PartitionOffset(partition = "*", initialOffset = "0")]
        )]
    )
    fun listen(pdlDocumentRecord: ConsumerRecord<String, String>) {
        runCatching {
            logger.debug("Reading offset ${pdlDocumentRecord.offset()} from partition ${pdlDocumentRecord.partition()} on kafka topic ${pdlDocumentRecord.topic()}")
            secureLogger.debug("Value is ${pdlDocumentRecord.value()}")
            val aktoerId = pdlDocumentRecord.key()
            if (pdlDocumentRecord.value() == null) {
                logger.debug("Received tombstone for aktoerId $aktoerId")
            } else {
                val pdlDokument: PdlDokument = ("{" + pdlDocumentRecord.value().substringAfter("{")).toPdlDokument()
                secureLogger.debug("Mottok pdldokument om $aktoerId med navn ${pdlDokument.hentPerson.navn}")
            }
        }.onFailure {
            secureLogger.error("Failed to process pdldokument record", it)
            //throw RuntimeException("Could not process pdldokument record. See more details in secure log.")
        }
    }

    private fun String.toPdlDokument(): PdlDokument = mapper.readValue(this, PdlDokument::class.java)
}
