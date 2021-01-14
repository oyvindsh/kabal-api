package no.nav.klage.oppgave.clients.pdl.graphql

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
    fun getPersonerInfo(fnrList: List<String>): HentPersonerResponse {
        val stsSystembrukerToken = tokenService.getStsSystembrukerToken()
        return pdlWebClient.post()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $stsSystembrukerToken")
            .header("Nav-Consumer-Token", "Bearer $stsSystembrukerToken")
            .bodyValue(hentPersonerQuery(fnrList))
            .retrieve()
            .bodyToMono<HentPersonerResponse>()
            .block() ?: throw RuntimeException("Person not found")
    }

    @Retryable
    fun getPersonInfo(fnr: String): HentPersonResponse {
        val stsSystembrukerToken = tokenService.getStsSystembrukerToken()
        return pdlWebClient.post()
            .header(HttpHeaders.AUTHORIZATION, "Bearer $stsSystembrukerToken")
            .header("Nav-Consumer-Token", "Bearer $stsSystembrukerToken")
            .bodyValue(hentPersonQuery(fnr))
            .retrieve()
            .bodyToMono<HentPersonResponse>()
            .block() ?: throw RuntimeException("Person not found")
    }
}
