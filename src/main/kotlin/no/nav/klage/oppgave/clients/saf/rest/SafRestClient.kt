package no.nav.klage.oppgave.clients.saf.rest

import brave.Tracer
import no.nav.klage.oppgave.service.TokenService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono


@Component
class SafRestClient(
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
    fun getDokument(
        dokumentInfoId: String,
        journalpostId: String,
        variantFormat: String
    ): ArkivertDokument {
        return try {
            runWithTimingAndLogging {
                safWebClient.get()
                    .uri(
                        "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}",
                        journalpostId,
                        dokumentInfoId,
                        variantFormat
                    )
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer ${tokenService.getSaksbehandlerAccessTokenWithSafScope()}"
                    )
                    .header("Nav-Callid", tracer.currentSpan().context().traceIdString())
                    .exchangeToMono {
                        it.wrapAsMono()
                    }
                    .block() ?: throw RuntimeException("no document data returned")
            }
        } catch (badRequest: WebClientResponseException.BadRequest) {
            logger.warn("Got a 400 fetching dokument with journalpostId $journalpostId, dokumentInfoId $dokumentInfoId and variantFormat $variantFormat")
            throw badRequest
        } catch (unautorized: WebClientResponseException.Unauthorized) {
            logger.warn("Got a 401 fetching dokument with journalpostId $journalpostId, dokumentInfoId $dokumentInfoId and variantFormat $variantFormat")
            throw unautorized
        } catch (forbidden: WebClientResponseException.Forbidden) {
            logger.warn("Got a 403 fetching dokument with journalpostId $journalpostId, dokumentInfoId $dokumentInfoId and variantFormat $variantFormat")
            throw forbidden
        } catch (notFound: WebClientResponseException.NotFound) {
            logger.warn("Got a 404 fetching dokument with journalpostId $journalpostId, dokumentInfoId $dokumentInfoId and variantFormat $variantFormat")
            throw notFound
        }
    }

    private fun ClientResponse.wrapAsMono(): Mono<ArkivertDokument> {
        val type = this.headers().header("Content-Type")
        val document = ArkivertDokument(
            bytes = this.bodyToMono<ByteArray>().block() ?: throw RuntimeException("no body found"),
            contentType = type.first())
        return Mono.just(document)
    }

    fun <T> runWithTimingAndLogging(block: () -> T): T {
        val start = System.currentTimeMillis()
        try {
            return block.invoke().let { secureLogger.debug("Received Dokument: $it"); it }
        } finally {
            val end = System.currentTimeMillis()
            logger.info("Time it took to call saf: ${end - start} millis")
        }
    }
}


