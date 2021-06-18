package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.ValgtEnhet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ValgtEnhetRepository : JpaRepository<ValgtEnhet, String> {
}
