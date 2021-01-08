package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Klagesak
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface KlagesakRepository : JpaRepository<Klagesak, UUID>
