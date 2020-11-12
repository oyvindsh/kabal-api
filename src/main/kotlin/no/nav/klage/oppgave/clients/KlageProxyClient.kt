package no.nav.klage.oppgave.clients

import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KlageProxyClient(
    private val klageProxyWebClient: WebClient,
    private val tokenValidationContextHolder: TokenValidationContextHolder
) {

    @Retryable
    //TODO: Ta i bruk @Cacheable
    fun getRoller(ident: String): List<String> {
        return klageProxyWebClient.get()
            .uri("/roller/{ident}", ident)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${getSaksbehandlersToken()}")
            .retrieve()
            .bodyToMono<List<String>>()
            .block()
            ?: throw RuntimeException("Unable to get roller for $ident")
    }

    private fun getSaksbehandlersToken() =
        tokenValidationContextHolder.tokenValidationContext.getJwtToken(SecurityConfiguration.ISSUER_AAD).tokenAsString

}
