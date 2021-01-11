package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Dokument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface DokumentRepository : JpaRepository<Dokument, Int> {

    fun findDokumenterForBehandling(behandlingId: UUID): List<Dokument>
}
