package no.nav.klage.oppgave.repositories

import jakarta.persistence.LockModeType
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface KlagebehandlingRepository : JpaRepository<Klagebehandling, UUID> {

    fun findByMottakId(mottakId: UUID): Klagebehandling?

    fun findByIdAndDelbehandlingerAvsluttetIsNotNull(id: UUID): Klagebehandling?

    fun findByDelbehandlingerAvsluttetIsNotNull(): List<Klagebehandling>

    fun findByDelbehandlingerAvsluttetIsNotNullAndSakenGjelderPartIdValue(partIdValue: String): List<Klagebehandling>

    fun findByKildeReferanseAndYtelse(kildeReferanse: String, ytelse: Ytelse): Klagebehandling?

    fun findByDvhReferanse(dvhReferanse: String): Klagebehandling?

    @Deprecated("See getOne")
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    override fun getOne(id: UUID): Klagebehandling

}
