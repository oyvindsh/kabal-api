package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) {
    fun getEnheterMedTemaerForSaksbehandler(): EnheterMedLovligeTemaer =
        innloggetSaksbehandlerRepository.getEnheterMedTemaerForSaksbehandler()
}