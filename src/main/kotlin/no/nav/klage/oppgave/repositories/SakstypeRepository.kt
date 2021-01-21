package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SakstypeRepository : JpaRepository<Sakstype, Int>
