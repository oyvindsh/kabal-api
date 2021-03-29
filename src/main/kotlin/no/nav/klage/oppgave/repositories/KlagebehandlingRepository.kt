package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*
import javax.persistence.LockModeType


@Repository
interface KlagebehandlingRepository : JpaRepository<Klagebehandling, UUID> {

    fun findByMottakId(mottakId: UUID): Klagebehandling?

    @Query("SELECT kb FROM Klagebehandling kb JOIN kb.vedtak v WHERE v.id = ?1")
    fun findByVedtakId(vedtakId: UUID): Klagebehandling?

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    override fun getOne(id: UUID): Klagebehandling
}
