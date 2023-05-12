package no.nav.klage.oppgave.repositories

import jakarta.persistence.LockModeType
import no.nav.klage.oppgave.domain.klage.Behandling
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*

interface BehandlingRepository : JpaRepository<Behandling, UUID> {

    fun findByDelbehandlingerAvsluttetIsNullAndDelbehandlingerAvsluttetAvSaksbehandlerIsNotNull(): List<Behandling>

    @EntityGraph(attributePaths = ["saksdokumenter", "hjemler", "delbehandlinger.hjemler", "delbehandlinger.medunderskriverHistorikk"])
    fun findByTildelingEnhetAndDelbehandlingerAvsluttetAvSaksbehandlerIsNull(enhet: String): List<Behandling>

    @Deprecated("See getOne")
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    override fun getOne(id: UUID): Behandling
}