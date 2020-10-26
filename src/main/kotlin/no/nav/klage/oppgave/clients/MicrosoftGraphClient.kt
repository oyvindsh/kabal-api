package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.domain.MicrosoftGraphIdentResponse
import no.nav.klage.oppgave.domain.MicrosoftGraphNameResponse
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MicrosoftGraphClient(
    private val microsoftGraphWebClient: WebClient,
    private val clientConfigurationProperties: ClientConfigurationProperties,
    private val oAuth2AccessTokenService: OAuth2AccessTokenService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Retryable
    fun getNavIdentForAuthenticatedUser(): String {
        logger.debug("Fetching navIdent from Microsoft Graph")

        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me")
                    .queryParam("\$select", "onPremisesSamAccountName")
                    .build()
            }.header("Authorization", "Bearer ${getSaksbehandlerTokenWithGraphScope()}")

            .retrieve()
            .bodyToMono<MicrosoftGraphIdentResponse>()
            .block()?.onPremisesSamAccountName ?: throw RuntimeException("NavIdent could not be fetched")
    }

    @Retryable
    fun getDisplayNames(idents: List<String>): Map<String, String> {
        return try {
            val response = microsoftGraphWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users")
                        .queryParam(
                            "\$filter",
                            "mailnickname in ${idents.joinToString(separator = "','", prefix = "('", postfix = "')")}"
                        )
                        .queryParam("\$select", "onPremisesSamAccountName,displayName")
                        .build()
                }.header("Authorization", "Bearer ${getAppTokenWithGraphScope()}")
                .retrieve()
                .bodyToMono<MicrosoftGraphNameResponse>()
                .block()

            response?.value?.mapNotNull {
                if (it.onPremisesSamAccountName == null || it.displayName == null) {
                    null
                } else {
                    it.onPremisesSamAccountName to it.displayName
                }
            }?.toMap() ?: emptyMap()
        } catch (e: Exception) {
            logger.warn("Could not fetch displayname for idents: $idents", e)
            emptyMap()
        }
    }

    private fun getSaksbehandlerTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    private fun getAppTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["app"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }
}