package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Kvalitetsskjema
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface KvalitetsskjemaRepository : JpaRepository<Kvalitetsskjema, UUID>