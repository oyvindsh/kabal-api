package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val microsoftGraphClient: MicrosoftGraphClient
) {

    fun getTilgangerForSaksbehandler() =
        saksbehandlerRepository.getTilgangerForSaksbehandler(getInnloggetIdent())

    fun getInnloggetIdent(): String {
        return microsoftGraphClient.getNavIdentForAuthenticatedUser()
    }

}