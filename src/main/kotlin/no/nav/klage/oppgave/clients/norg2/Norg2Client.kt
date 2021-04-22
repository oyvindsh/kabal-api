package no.nav.klage.oppgave.clients.norg2

import no.nav.klage.oppgave.config.CacheWithJCacheConfiguration.Companion.ENHET_CACHE
import no.nav.klage.oppgave.util.getLogger
import org.springframework.cache.annotation.Cacheable
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class Norg2Client(private val norg2WebClient: WebClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Retryable
    @Cacheable(ENHET_CACHE)
    fun fetchEnhet(enhetNr: String): Enhet =
        norg2WebClient.get()
            .uri("/enhet/{enhetNr}", enhetNr)
            .retrieve()
            .bodyToMono<EnhetResponse>()
            .block()
            ?.asEnhet() ?: Enhet(navn = "Ukjent enhet").also { logger.warn("Enhet not found for $enhetNr") }

}
