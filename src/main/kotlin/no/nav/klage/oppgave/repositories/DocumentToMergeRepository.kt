package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.DocumentToMerge
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface DocumentToMergeRepository : JpaRepository<DocumentToMerge, UUID> {

    fun findByReferenceIdOrderByIndex(referenceId: UUID): Set<DocumentToMerge>

    @Modifying
    @Query("""
        delete from DocumentToMerge where created < :date
    """)
    fun deleteByCreatedBefore(date: LocalDateTime)

}