package no.nav.klage.dokument.clients.kabaljsontopdf

import brave.Tracer
import no.nav.klage.dokument.domain.PDFDocument
import no.nav.klage.dokument.exceptions.DokumentValidationException
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono


@Component
class KabalJsonToPdfClient(
    private val kabalJsonToPdfWebClient: WebClient,
    private val tracer: Tracer
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getPDFDocument(json: String): PDFDocument {
        logger.debug("Getting pdf document from kabalJsontoPdf.")
        return kabalJsonToPdfWebClient.post()
            .uri { it.path("/topdf").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .retrieve()
            .toEntity(ByteArray::class.java)
            .map {
                val filename = it.headers["filename"]?.first()
                PDFDocument(
                    filename = filename
                        ?: "somefilename",
                    bytes = it.body ?: throw RuntimeException("Could not get PDF data")
                )
            }
            .block() ?: throw RuntimeException("PDF could not be created")
    }

    fun validateJsonDocument(json: String) {
        logger.debug("Validating json document in kabalJsontoPdf.")
        return kabalJsonToPdfWebClient.post()
            .uri { it.path("/validate").build() }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(json)
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .retrieve()
            .onStatus(
                { status: HttpStatus -> status.isError },
                { errorResponse: ClientResponse ->
                    errorResponse.bodyToMono<String>().flatMap { errorBody ->
                        logger.error("Feilet med å validere dokument. Feil: {}", errorBody)
                        Mono.error(DokumentValidationException(errorBody))
                    }
                }
            )

            .bodyToMono<Unit>()
            .block() ?: throw RuntimeException("Document Json could not be validated")
    }
}