package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.KafkaVedtakEvent
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface KafkaVedtakEventRepository : JpaRepository<KafkaVedtakEvent, UUID> {
    fun getAllByStatusIsNotLikeSENDT(): List<KafkaVedtakEvent>
}
