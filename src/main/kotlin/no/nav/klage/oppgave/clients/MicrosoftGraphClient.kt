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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

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
    fun getAll(idents: List<List<String>>): Map<String, String> {
        val queryString = idents.map {
            it.joinToString(separator = "','", prefix = "('", postfix = "')")
        }

        val data = Flux.fromIterable(queryString)
            .parallel()
            .runOn(Schedulers.elastic())
            .flatMap {
                getDisplayNames(it)
            }
            .ordered { u1: MicrosoftGraphNameResponse, u2: MicrosoftGraphNameResponse -> 1 }.toIterable()

        return data.flatMap {
            it.value ?: emptyList()
        }.mapNotNull {
            if (it.onPremisesSamAccountName == null || it.displayName == null) {
                null
            } else {
                it.onPremisesSamAccountName to it.displayName
            }
        }.toMap()
    }

    private fun getDisplayNames(idents: String): Mono<MicrosoftGraphNameResponse> {
        return try {
            microsoftGraphWebClient.get()
                .uri { uriBuilder ->
                    uriBuilder
                        .path("/users")
                        .queryParam("\$filter", "mailnickname in $idents")
                        .queryParam("\$select", "onPremisesSamAccountName,displayName")
                        .build()
                }.header("Authorization", "Bearer ${getAppTokenWithGraphScope()}")
                .retrieve()
                .bodyToMono()
        } catch (e: Exception) {
            logger.warn("Could not fetch displayname for idents: $idents", e)
            Mono.empty()
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