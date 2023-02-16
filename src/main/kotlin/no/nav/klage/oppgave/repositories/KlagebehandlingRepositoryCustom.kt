package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.klage.Klagebehandling


interface KlagebehandlingRepositoryCustom {

    fun getAnkemuligheter(partIdValue: String): List<Klagebehandling>
}