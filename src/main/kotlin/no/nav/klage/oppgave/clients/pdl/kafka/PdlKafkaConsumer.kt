package no.nav.klage.oppgave.clients.pdl.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.oppgave.config.PartitionFinder
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.event.ListenerContainerIdleEvent
import org.springframework.stereotype.Component


@Component
class PdlKafkaConsumer(
    private val pdlPersonService: PersonCacheService,
    @Qualifier("pdlPersonFinder") private val pdlPersonFinder: PartitionFinder<Any, Any>
) {

    private var kafkaConsumerHasReadAllMsgs = false;

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    @KafkaListener(
        id = "klagePdlPersonListener",
        idIsGroup = false,
        containerFactory = "pdlPersonKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(
            topic = "\${PDL_PERSON_KAFKA_TOPIC}",
            partitions = ["#{@pdlPersonFinder.partitions('\${PDL_PERSON_KAFKA_TOPIC}')}"],
            partitionOffsets = [PartitionOffset(partition = "*", initialOffset = "0")]
        )]
    )
    fun listen(pdlDocumentRecord: ConsumerRecord<String, String>) {
        runCatching {
            logger.debug("Reading offset ${pdlDocumentRecord.offset()} from partition ${pdlDocumentRecord.partition()} on kafka topic ${pdlDocumentRecord.topic()}")
            val aktoerId = pdlDocumentRecord.key()
            if (pdlDocumentRecord.value() == null) {
                logger.debug("Received tombstone for aktoerId $aktoerId")
            } else {
                val pdlDokument: PdlDokument = pdlDocumentRecord.value().toPdlDokument()
                val pdlPerson = PdlDokumentMapper.mapToPerson(pdlDokument)
                pdlPerson?.let { pdlPersonService.oppdaterPersonCache(it) }
            }
        }.onFailure {
            secureLogger.error("Failed to process pdldokument record", it)
            throw RuntimeException("Could not process pdldokument record. See more details in secure log.")
        }
    }

    @EventListener(condition = "event.listenerId.startsWith('klagePdlPersonListener-')")
    fun eventHandler(event: ListenerContainerIdleEvent) {
        if (!kafkaConsumerHasReadAllMsgs) {
            logger.debug("Mottok ListenerContainerIdleEvent fra klagePdlPersonListener")
        }
        kafkaConsumerHasReadAllMsgs = true
    }

    private fun String.toPdlDokument(): PdlDokument = mapper.readValue(this, PdlDokument::class.java)

}




