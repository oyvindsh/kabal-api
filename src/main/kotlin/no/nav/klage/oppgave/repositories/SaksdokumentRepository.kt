package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Saksdokument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SaksdokumentRepository : JpaRepository<Saksdokument, UUID> {

    fun findByKlagebehandlingId(klagebehandlingId: UUID): List<Saksdokument>

    fun existsByKlagebehandlingIdAndReferanse(klagebehandlingId: UUID, referanse: String): Boolean

    fun findByKlagebehandlingIdAndReferanse(klagebehandlingId: UUID, journalpostId: String): Saksdokument?
}
