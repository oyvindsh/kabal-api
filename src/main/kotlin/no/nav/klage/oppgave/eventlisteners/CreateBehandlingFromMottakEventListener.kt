package no.nav.klage.oppgave.eventlisteners

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.events.MottakLagretEvent
import no.nav.klage.oppgave.domain.kafka.*
import no.nav.klage.oppgave.domain.klage.Ankebehandling
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.repositories.KafkaEventRepository
import no.nav.klage.oppgave.service.AnkebehandlingService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class CreateBehandlingFromMottakEventListener(
    private val klagebehandlingService: KlagebehandlingService,
    private val ankebehandlingService: AnkebehandlingService,
    private val behandlingRepository: BehandlingRepository,
    private val kafkaEventRepository: KafkaEventRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val objectMapperBehandlingEvents = ObjectMapper().registerModule(JavaTimeModule()).configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false
        )
    }

    @EventListener
    fun createBehandling(mottakLagretEvent: MottakLagretEvent): Behandling {
        logger.debug("Received MottakLagretEvent for mottak ${mottakLagretEvent.mottak.id} in CreateKlagebehandlingFromMottakEventListener")
        val mottakId = mottakLagretEvent.mottak.id
        //TODO
//        if (behandlingRepository.findByMottakId(mottakId) != null) {
//            logger.error("We already have a behandling for mottak ${mottakId}. This is not supposed to happen.")
//            throw RuntimeException("We already have a behandling for mottak $mottakId")
//        }

        return when (mottakLagretEvent.mottak.type) {
            Type.KLAGE -> klagebehandlingService.createKlagebehandlingFromMottak(mottakLagretEvent.mottak)
            Type.ANKE -> {
                val ankebehandling = ankebehandlingService.createAnkebehandlingFromMottak(mottakLagretEvent.mottak)
                publishKafkaEvent(ankebehandling)
                ankebehandling
            }

            Type.ANKE_I_TRYGDERETTEN -> TODO()
        }
    }

    private fun publishKafkaEvent(ankebehandling: Ankebehandling) {
        //Publiser Kafka-event, infomelding om opprettelse
        val behandlingEvent = BehandlingEvent(
            eventId = UUID.randomUUID(),
            kildeReferanse = ankebehandling.kildeReferanse,
            kilde = ankebehandling.fagsystem.navn,
            kabalReferanse = ankebehandling.id.toString(),
            type = BehandlingEventType.ANKEBEHANDLING_OPPRETTET,
            detaljer = BehandlingDetaljer(
                ankebehandlingOpprettet =
                AnkebehandlingOpprettetDetaljer(
                    mottattKlageinstans = ankebehandling.created
                )
            )
        )
        kafkaEventRepository.save(
            KafkaEvent(
                id = UUID.randomUUID(),
                behandlingId = ankebehandling.id,
                kilde = ankebehandling.fagsystem.navn,
                kildeReferanse = ankebehandling.kildeReferanse,
                jsonPayload = objectMapperBehandlingEvents.writeValueAsString(behandlingEvent),
                type = EventType.BEHANDLING_EVENT
            )
        )
    }
}