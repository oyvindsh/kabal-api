package no.nav.klage.oppgave.config

import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import java.io.File


@Configuration
class KafkaConfiguration(
    @Value("\${KAFKA_BOOTSTRAP_SERVERS}")
    private val bootstrapServers: String,
    @Value("\${KAFKA_GROUP_ID}")
    private val groupId: String,
    @Value("\${SERVICE_USER_USERNAME}")
    private val username: String,
    @Value("\${SERVICE_USER_PASSWORD}")
    private val password: String,
    @Value("\$KAFKA_SCHEMA_REGISTRY_URL")
    private val schemaRegistryUrl
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Bean
    fun egenAnsattKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, String>()
        factory.consumerFactory = egenAnsattConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.idleEventInterval = 3000L
        factory.setErrorHandler { thrownException, data ->
            logger.error("Could not deserialize record. See secure logs for details.")
            secureLogger.error("Could not deserialize record: $data", thrownException)
        }

        return factory
    }

    @Bean
    fun pdlPersonKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        factory.consumerFactory = pdlPersonConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.idleEventInterval = 3000L
        factory.setErrorHandler { thrownException, data ->
            logger.error("Could not deserialize record. See secure logs for details.")
            secureLogger.error("Could not deserialize record: $data", thrownException)
        }

        return factory
    }

    @Bean
    fun egenAnsattConsumerFactory(): ConsumerFactory<String, String> {
        return DefaultKafkaConsumerFactory(egenAnsattConsumerProps())
    }

    @Bean
    fun pdlPersonConsumerFactory(): ConsumerFactory<Any, Any> {
        return DefaultKafkaConsumerFactory(pdlPersonConsumerProps())
    }

    private fun egenAnsattConsumerProps(): Map<String, Any> {
        val props = mutableMapOf<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props["spring.deserializer.key.delegate.class"] = StringDeserializer::class.java
        props["spring.deserializer.value.delegate.class"] = StringDeserializer::class.java
        props[SaslConfigs.SASL_JAAS_CONFIG] =
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        props[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        System.getenv("NAV_TRUSTSTORE_PATH")?.let {
            props[SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = File(it).absolutePath
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("NAV_TRUSTSTORE_PASSWORD")
        }
        return props
    }

    private fun pdlPersonConsumerProps(): Map<String, Any> {
        val props = mutableMapOf<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        props[SPECIFIC_AVRO_READER_CONFIG] = false
        props[SCHEMA_REGISTRY_URL_CONFIG] = schemaRegistryUrl
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        props["spring.deserializer.key.delegate.class"] = KafkaAvroDeserializer::class.java
        props["spring.deserializer.value.delegate.class"] = KafkaAvroDeserializer::class.java
        props[SaslConfigs.SASL_JAAS_CONFIG] =
            "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        props[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        System.getenv("NAV_TRUSTSTORE_PATH")?.let {
            props[SECURITY_PROTOCOL_CONFIG] = "SASL_SSL"
            props[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = File(it).absolutePath
            props[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = System.getenv("NAV_TRUSTSTORE_PASSWORD")
        }
        return props
    }

    @Bean
    fun egenAnsattFinder(): PartitionFinder<String, String> {
        return PartitionFinder(egenAnsattConsumerFactory())
    }

    @Bean
    fun pdlPersonFinder(): PartitionFinder<Any, Any> {
        return PartitionFinder(pdlPersonConsumerFactory())
    }

}

class PartitionFinder<K, V>(private val consumerFactory: ConsumerFactory<K, V>) {
    fun partitions(topic: String): Array<String> {
        consumerFactory.createConsumer().use { consumer ->
            return consumer.partitionsFor(topic)
                .map { pi -> "" + pi.partition() }
                .toTypedArray()
        }
    }
}
