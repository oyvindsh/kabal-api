package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.KafkaDVHEvent
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface KafkaDVHEventRepository : JpaRepository<KafkaDVHEvent, UUID> {
    fun getAllByStatusIsNotLike(status: UtsendingStatus): List<KafkaDVHEvent>
}
