package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository
) {
    fun getEnheterMedTemaerForSaksbehandler(): EnheterMedLovligeTemaer =
        innloggetSaksbehandlerRepository.getEnheterMedTemaerForSaksbehandler()

    fun getMedunderskrivere(ident: String, tema: String): Medunderskrivere {
        val medunderskrivere = saksbehandlerRepository.getAlleSaksbehandlerIdenter()
            .filter { it != ident }
            .filter { saksbehandlerHarTilgangTilTema(it, tema) }
            .map { Medunderskriver(it, getNameForIdent(it)) }
        return Medunderskrivere(tema, medunderskrivere)
    }

    private fun saksbehandlerHarTilgangTilTema(ident: String, tema: String) =
        saksbehandlerRepository.getEnheterMedTemaerForSaksbehandler(ident).enheter.flatMap { it.temaer }
            .contains(Tema.of(tema))

    private fun getNameForIdent(it: String) =
        saksbehandlerRepository.getNamesForSaksbehandlere(setOf(it)).getOrDefault(it, "Ukjent navn")
}