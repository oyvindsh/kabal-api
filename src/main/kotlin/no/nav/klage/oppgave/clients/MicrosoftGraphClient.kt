package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.domain.MicrosoftGraphIdentResponse
import no.nav.klage.oppgave.domain.MicrosoftGraphNameResponse
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class MicrosoftGraphClient(private val microsoftGraphWebClient: WebClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()
    }

    fun getNavIdent(accessToken: String): String {
        logger.debug("Fetching navIdent from Microsoft Graph")

        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/me")
                    .queryParam("\$select", "onPremisesSamAccountName")
                    .build()
            }.header("Authorization", "Bearer $accessToken")

            .retrieve()
            .bodyToMono<MicrosoftGraphIdentResponse>()
            .block()?.onPremisesSamAccountName ?: throw RuntimeException("NavIdent could not be fetched")
    }

    fun getNamesForSaksbehandlere(identer: Set<String>, accessToken: String): Map<String, String> {
        logger.debug("Fetching names for saksbehandlere from Microsoft Graph. Identer: {}", identer)

        val identerNotInCache = identer.toMutableSet()
        identerNotInCache.removeAll(saksbehandlerNameCache.keys)
        logger.debug("Fetching identer not in cache: {}", identerNotInCache)

        identerNotInCache.map {
            it to getDisplayName(it, accessToken)
        }.toMap()

        return saksbehandlerNameCache
    }

    private fun getDisplayName(ident: String, accessToken: String): String {
        return microsoftGraphWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/users/{ident}")
                    .queryParam("\$select", "displayName")
                    .build(ident)
            }.header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono<MicrosoftGraphNameResponse>()
            .block()?.displayName ?: "mangler"
    }
}