package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.domain.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.klage.ValgtEnhet
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.repositories.ValgtEnhetRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SaksbehandlerService(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val valgtEnhetRepository: ValgtEnhetRepository
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

    fun getNamesForSaksbehandlere(idents: Set<String>) =
        saksbehandlerRepository.getNamesForSaksbehandlere(idents)

    @Transactional
    fun storeValgtEnhetId(ident: String, enhetId: String): EnhetMedLovligeTemaer {
        val enhet = getEnheterMedTemaerForSaksbehandler().enheter.find { it.enhetId == enhetId }
            ?: throw MissingTilgangException("Saksbehandler $ident har ikke tilgang til enhet $enhetId")

        valgtEnhetRepository.save(
            mapToValgtEnhet(ident, enhet)
        )
        return enhet
    }

    private fun mapToValgtEnhet(ident: String, enhet: EnhetMedLovligeTemaer): ValgtEnhet {
        return ValgtEnhet(
            saksbehandlerident = ident,
            enhetId = enhet.enhetId,
            enhetNavn = enhet.navn,
            tidspunkt = LocalDateTime.now()
        )
    }

    @Transactional
    fun findValgtEnhet(ident: String): EnhetMedLovligeTemaer {
        return valgtEnhetRepository.findByIdOrNull(ident)
            ?.let { valgtEnhet -> getEnheterMedTemaerForSaksbehandler().enheter.find { it.enhetId == valgtEnhet.enhetId } }
            ?: storeValgtEnhetId(ident, getEnheterMedTemaerForSaksbehandler().enheter.first().enhetId)
    }
}
