package no.nav.klage.oppgave.clients.pdl

import no.nav.klage.oppgave.service.TokenService
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class PdlClient(
    private val pdlWebClient: WebClient,
    private val tokenService: TokenService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun getPersonInfo(fnrList: List<String>): HentPersonResponse {
        val stsSystembrukerToken = tokenService.getStsSystembrukerToken()
        val accessTokenFrontendSent = tokenService.getAccessTokenFrontendSent()
        secureLogger.debug("systembrukertoken: $stsSystembrukerToken")
        secureLogger.debug("innloggetbrukertoken: $accessTokenFrontendSent")
        return pdlWebClient.post()
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer $accessTokenFrontendSent"
            )
            .header("Nav-Consumer-Token", "Bearer $stsSystembrukerToken")
            .bodyValue(hentPersonQuery(fnrList))
            .retrieve()
            .bodyToMono<HentPersonResponse>()
            .block() ?: throw RuntimeException("Person not found")
    }
}
