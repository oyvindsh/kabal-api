package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Behandlingslogg
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BehandlingsloggRepository : JpaRepository<Behandlingslogg, Int>
