package no.nav.klage.oppgave.service.unleash

import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service

@Service
class TokenUtils(
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
    val microsoftGraphClient: MicrosoftGraphClient
) {

    private fun getTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getInnloggetIdent(): String {
        return microsoftGraphClient.getNavIdent(getTokenWithGraphScope())
    }
}