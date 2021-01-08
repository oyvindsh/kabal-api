package no.nav.klage.oppgave.egenansatt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.stereotype.Component


@Component
class EgenAnsattKafkaConsumer(private val egenAnsattService: EgenAnsattService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    /*
    TODO: Prøve å gjøre oppsettet dynamisk, ref https://docs.spring.io/spring-kafka/reference/html/#tip-assign-all-parts
    Husk denne: https://stackoverflow.com/questions/44506762/kotlin-compiler-complains-about-using-a-spel-expression-in-a-property-definition
     */
    @KafkaListener(
        topicPartitions = [TopicPartition(
            topic = "\${EGENANSATT_KAFKA_TOPIC}",
            partitionOffsets = [PartitionOffset(partition = "0", initialOffset = "0")]
        )]
    )
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
}
