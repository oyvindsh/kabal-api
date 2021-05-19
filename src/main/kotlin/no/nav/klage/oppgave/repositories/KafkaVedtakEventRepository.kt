package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.KafkaVedtakEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface KafkaVedtakEventRepository : JpaRepository<KafkaVedtakEvent, UUID> {
    @Query("SELECT k FROM KafkaVedtakEvent k WHERE k.status <> 'SENDT'")
    fun fetchAllVedtakIkkeSendt(): List<KafkaVedtakEvent>
}
