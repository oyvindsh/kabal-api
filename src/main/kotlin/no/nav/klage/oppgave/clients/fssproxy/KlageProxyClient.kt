package no.nav.klage.oppgave.clients.fssproxy

import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.ROLLER_CACHE
import no.nav.klage.oppgave.service.TokenService
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KlageProxyClient(
    private val klageProxyWebClient: WebClient,
    private val tokenService: TokenService
) {

    @Retryable
    @Cacheable(ROLLER_CACHE)
    fun getRoller(ident: String): List<String> {
        return klageProxyWebClient.get()
            .uri("/roller/{ident}", ident)
            .header(HttpHeaders.AUTHORIZATION, "Bearer ${tokenService.getSaksbehandlersTokenWithProxyScope()}")
            .retrieve()
            .bodyToMono<List<String>>()
            .block()
            ?: throw RuntimeException("Unable to get roller for $ident")
    }
}
