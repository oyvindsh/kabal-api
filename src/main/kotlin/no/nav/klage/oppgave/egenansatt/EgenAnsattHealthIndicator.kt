package no.nav.klage.oppgave.egenansatt

import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.event.EventListener
import org.springframework.kafka.event.ListenerContainerIdleEvent
import org.springframework.stereotype.Component

@Component
class EgenAnsattHealthIndicator : HealthIndicator {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private var kafkaConsumerHasReadAllMsgs = false;


    @EventListener
    fun eventHandler(event: ListenerContainerIdleEvent) {
        kafkaConsumerHasReadAllMsgs = true
    }

    override fun health(): Health =
        if (kafkaConsumerHasReadAllMsgs) {
            Health.up().build()
        } else {
            Health.down().build()
        }
}
