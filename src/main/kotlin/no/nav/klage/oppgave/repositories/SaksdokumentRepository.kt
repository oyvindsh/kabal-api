package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Saksdokument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SaksdokumentRepository : JpaRepository<Saksdokument, UUID> {

    fun findDokumenterForBehandling(behandlingId: UUID): List<Saksdokument>
}
