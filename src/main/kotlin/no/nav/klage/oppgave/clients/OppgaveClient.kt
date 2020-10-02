package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.OppgaveResponse
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val stsClient: StsClient,
    private val tracer: Tracer
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${spring.application.name}")
    lateinit var applicationName: String

    fun getOppgaver(): String {
        logger.debug("Fetching oppgaver")

        val oidcToken = stsClient.oidcToken()
        logger.debug("oidc token: {}", oidcToken)

        return oppgaveWebClient.get()
            .uri { uriBuilder ->
                uriBuilder
                    //Try to limit amount when testing
                    .queryParam("opprettetFom", "2020-10-01T07:00:00")
                    .build()
            }
            .header("Authorization", "Bearer $oidcToken")
//            .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .retrieve()
            .bodyToMono<String>()
            .block() ?: throw RuntimeException("Oppgaver could not be fetched")
    }
}



