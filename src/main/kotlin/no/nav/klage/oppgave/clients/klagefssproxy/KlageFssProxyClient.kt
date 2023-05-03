package no.nav.klage.oppgave.clients.klagefssproxy

import no.nav.klage.oppgave.clients.klagefssproxy.domain.HandledInKabalInput
import no.nav.klage.oppgave.clients.klagefssproxy.domain.SakAssignedInput
import no.nav.klage.oppgave.clients.klagefssproxy.domain.SakFinishedInput
import no.nav.klage.oppgave.clients.klagefssproxy.domain.SakFromKlanke
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class KlageFssProxyClient(
    private val klageFssProxyWebClient: WebClient,
    private val tokenUtil: TokenUtil,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getSak(sakId: String): SakFromKlanke {
        return klageFssProxyWebClient.get()
            .uri { it.path("/klanke/saker/{sakId}").build(sakId) }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getOnBehalfOfTokenWithKlageFSSProxyScope()}"
            )
            .retrieve()
            .bodyToMono<SakFromKlanke>()
            .block()
            ?: throw RuntimeException("Empty result")
    }

    fun setToHandledInKabal(sakId: String, input: HandledInKabalInput) {
        klageFssProxyWebClient.post()
            .uri { it.path("/klanke/saker/{sakId}/handledinkabal").build(sakId) }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getOnBehalfOfTokenWithKlageFSSProxyScope()}"
            )
            .bodyValue(input)
            .retrieve()
            .bodyToMono<Unit>()
            .block()
    }

    fun setToFinished(sakId: String, input: SakFinishedInput) {
        klageFssProxyWebClient.post()
            .uri { it.path("/klanke/saker/{sakId}/finished").build(sakId) }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getAppAccessTokenWithKlageFSSProxyScope()}"
            )
            .bodyValue(input)
            .retrieve()
            .bodyToMono<Unit>()
            .block()
    }

    fun setToAssigned(sakId: String, input: SakAssignedInput) {
        klageFssProxyWebClient.post()
            .uri { it.path("/klanke/saker/{sakId}/assignedinkabal").build(sakId) }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getAppAccessTokenWithKlageFSSProxyScope()}"
            )
            .bodyValue(input)
            .retrieve()
            .bodyToMono<Unit>()
            .block()
    }
}