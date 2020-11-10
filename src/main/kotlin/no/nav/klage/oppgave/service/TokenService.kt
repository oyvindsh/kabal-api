package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.StsClient
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService,
    private val stsClient: StsClient
) {

    fun getSaksbehandlerTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["azure-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getSaksbehandlerTokenWithOppgaveScope(): String {
        val clientProperties = clientConfigurationProperties.registration["oppgave-onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getAppTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["app"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    fun getStsSystembrukerToken(): String = stsClient.oidcToken()
}