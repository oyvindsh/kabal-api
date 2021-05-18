package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import org.assertj.core.api.Assertions.assertThat
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.stereotype.Component
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(
    classes = [
        VedtakKafkaProducer::class,
        KafkaAutoConfiguration::class,
        KafkaConsumer::class
    ]
)
@ActiveProfiles("local")
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = ["listeners=PLAINTEXT://localhost:9092", "port=9092"] )
class VedtakProducerTest {

    @Autowired
    lateinit var vedtakProducer: VedtakKafkaProducer

    @Autowired
    lateinit var kafkaConsumer: KafkaConsumer

    @Test
    fun `payload sent properly`() {
        vedtakProducer.sendVedtak(KlagevedtakFattet(
            kilde = "Test",
            kildeReferanse = "Testref",
            utfall = Utfall.MEDHOLD,
            vedtaksbrevReferanse = "Brev1",
            kabalReferanse = "12345"
        ))
        kafkaConsumer.latch.await(1000, TimeUnit.MILLISECONDS)

        assertThat(kafkaConsumer.payload?.contains("Testref") ?: false)
    }

}

@Component
class KafkaConsumer {
    val latch = CountDownLatch(1)
    var payload: String? = null

    @KafkaListener(topics = ["\${VEDTAK_FATTET_TOPIC}"])
    fun receive(consumerRecord: ConsumerRecord<*, *>) {
        println("received payload='${consumerRecord}'")
        payload = consumerRecord.toString()
        latch.countDown()
    }
}
