package no.nav.klage.oppgave.pdl

import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.context.event.EventListener
import org.springframework.kafka.event.ListenerContainerIdleEvent
import org.springframework.stereotype.Component

@Component
class PdlPersonHealthIndicator : HealthIndicator {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    private var kafkaConsumerHasReadAllMsgs = true;


    @EventListener(condition = "event.listenerId.startsWith('klagePdlPersonListener-')")
    fun eventHandler(event: ListenerContainerIdleEvent) {
        logger.debug("Mottok ListenerContainerIdleEvent fra klagePdlPersonListener")
        kafkaConsumerHasReadAllMsgs = true
    }

    override fun health(): Health =
        if (kafkaConsumerHasReadAllMsgs) {
            Health.up().build()
        } else {
            Health.down().build()
        }
}
