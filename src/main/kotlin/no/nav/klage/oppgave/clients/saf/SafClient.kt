package no.nav.klage.oppgave.clients.saf

import brave.Tracer
import no.nav.klage.oppgave.service.TokenService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Component
class SafClient(
    private val safWebClient: WebClient,
    private val tokenService: TokenService,
    private val tracer: Tracer,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Retryable
    fun getDokumentoversiktBruker(
        fnr: String,
        pageSize: Int,
        previousPageRef: String? = null
    ): DokumentoversiktBrukerResponse {
        return runWithTimingAndLogging {
            safWebClient.post()
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer ${tokenService.getSaksbehandlerAccessTokenWithOppgaveScope()}"
                )
                .header("Nav-Callid", tracer.currentSpan().context().traceIdString())

                .bodyValue(hentDokumentoversiktBrukerQuery(fnr, pageSize, previousPageRef))
                .retrieve()
                .bodyToMono<DokumentoversiktBrukerResponse>()
                .block() ?: throw RuntimeException("getDokumentoversiktBruker failed")
        }
    }

    fun <T> runWithTimingAndLogging(block: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return block.invoke().let { secureLogger.debug("Received DokumentoversiktBruker: $it"); it }
        } finally {
            val end = System.currentTimeMillis()
            logger.info("Time it took to call saf: ${end - start} millis")
        }
    }
}