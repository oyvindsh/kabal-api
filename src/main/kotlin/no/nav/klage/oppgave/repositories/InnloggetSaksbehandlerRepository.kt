package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val microsoftGraphClient: MicrosoftGraphClient,
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

    fun getTilgangerForSaksbehandler() =
        saksbehandlerRepository.getTilgangerForSaksbehandler(getInnloggetIdent())

    fun getInnloggetIdent(): String {
        return getNavIdent(getSaksbehandlerTokenWithGraphScope())
    }

    private fun getNavIdent(accessToken: String): String =
        microsoftGraphClient.getNavIdent(accessToken)

    private fun getSaksbehandlerTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }
}