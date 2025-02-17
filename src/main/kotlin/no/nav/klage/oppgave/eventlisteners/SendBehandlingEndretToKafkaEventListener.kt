package no.nav.klage.oppgave.eventlisteners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.kafka.Event
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandling
import no.nav.klage.oppgave.domain.klage.Ankebehandling
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.service.BehandlingEndretKafkaProducer
import no.nav.klage.oppgave.service.KafkaInternalEventService
import no.nav.klage.oppgave.util.getLogger
import org.hibernate.Hibernate
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Service
class SendBehandlingEndretToKafkaEventListener(
    private val behandlingEndretKafkaProducer: BehandlingEndretKafkaProducer,
    private val kafkaInternalEventService: KafkaInternalEventService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        val objectMapper: ObjectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    /* Denne kjøres utenfor transaksjonen. Trenger man at dette kjøres i en transaksjon, kan man bruke @Transactional(propagation = Propagation.REQUIRES_NEW)  eller en kombinasjon av @Transactional og @Async */
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun indexKlagebehandling(behandlingEndretEvent: BehandlingEndretEvent) {
        logger.debug("Received BehandlingEndretEvent for behandlingId {}", behandlingEndretEvent.behandling.id)
        val behandling = Hibernate.unproxy(behandlingEndretEvent.behandling) as Behandling
        try {
            when (behandling.type) {
                Type.KLAGE ->
                    behandlingEndretKafkaProducer.sendKlageEndretV2(behandling as Klagebehandling)
                Type.ANKE ->
                    behandlingEndretKafkaProducer.sendAnkeEndretV2(behandling as Ankebehandling)
                Type.ANKE_I_TRYGDERETTEN ->
                    behandlingEndretKafkaProducer.sendAnkeITrygderettenEndretV2(behandling as AnkeITrygderettenbehandling)
            }
        } catch (e: Exception) {
            logger.error("could not index behandling with id ${behandlingEndretEvent.behandling.id}", e)
        }
    }

    //POC for updating FE
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishInternalEvent(behandlingEndretEvent: BehandlingEndretEvent) {
        logger.debug("Publishing internal event based on BehandlingEndretEvent for behandlingId {}", behandlingEndretEvent.behandling.id)
        behandlingEndretEvent.endringslogginnslag.forEach {
            kafkaInternalEventService.publishEvent(
                Event(
                    behandlingId = behandlingEndretEvent.behandling.id.toString(),
                    name = it.felt.toString(),
                    id = "",
                    data = objectMapper.writeValueAsString(it.tilVerdi),
                )
            )
        }
    }
}