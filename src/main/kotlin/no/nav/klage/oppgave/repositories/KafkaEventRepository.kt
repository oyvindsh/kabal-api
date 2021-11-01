package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.kafka.EventType
import no.nav.klage.oppgave.domain.kafka.KafkaEvent
import no.nav.klage.oppgave.domain.kafka.UtsendingStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface KafkaEventRepository : JpaRepository<KafkaEvent, UUID> {
    fun getAllByStatusIsNotLikeAndTypeIsLikeOrderByCreated(status: UtsendingStatus, type: EventType): List<KafkaEvent>
}
