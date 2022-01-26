package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.saksbehandler.Enhet
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class SaksbehandlerService(
    private val saksbehandlerRepository: SaksbehandlerRepository,
) {
    //TODO: Jeg tenker vi kan/bør flytte innholdet i SaksbehandlerRepository inn hit, og så slette SaksbehandlerRepository. Jeg har aldri vært helt komfy med at det er et Repository, det er bedre at det er en Service tenker jeg.

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getEnhetForSaksbehandler(navIdent: String): Enhet = saksbehandlerRepository.getEnhetForSaksbehandler(navIdent)

    fun getNameForIdent(it: String) =
        saksbehandlerRepository.getNamesForSaksbehandlere(setOf(it)).getOrDefault(it, "Ukjent navn")

}
