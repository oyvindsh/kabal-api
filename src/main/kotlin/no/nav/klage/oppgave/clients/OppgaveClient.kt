package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.exceptions.OppgaveNotFoundException
import no.nav.klage.oppgave.service.OppgaveSearchCriteria
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriBuilder
import java.lang.System.currentTimeMillis
import java.net.URI

@Component
class OppgaveClient(
    private val oppgaveWebClient: WebClient,
    private val stsClient: StsClient,
    private val tracer: Tracer,
    @Value("\${spring.application.name}") val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()

        const val TEMA_SYK = "SYK"
        const val STATUSKATEGORI_AAPEN = "AAPEN"
        const val HJEMMEL = "HJEMMEL"

    }

    @Retryable
    fun getOnePage(offset: Int): OppgaveResponse {
        return logTimingAndWebClientResponseException {
            oppgaveWebClient.get()
                .uri { uriBuilder ->
                    buildDefaultUri(uriBuilder, offset)
                }
                .header("Authorization", "Bearer ${stsClient.oidcToken()}")
                .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<OppgaveResponse>()
                .block() ?: throw RuntimeException("Oppgaver could not be fetched")
        }
    }

    @Retryable
    fun getOneSearchPage(oppgaveSearchCriteria: OppgaveSearchCriteria, offset: Int): OppgaveResponse {
        return logTimingAndWebClientResponseException {
            oppgaveWebClient.get()
                .uri { uriBuilder -> oppgaveSearchCriteria.buildUri(uriBuilder, offset) }
                .header("Authorization", "Bearer ${stsClient.oidcToken()}")
                .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<OppgaveResponse>()
                .block() ?: throw RuntimeException("Oppgaver could not be fetched")
        }
    }

    private fun buildDefaultUri(uriBuilder: UriBuilder, offset: Int): URI {
        return uriBuilder
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
            .queryParam("tema", TEMA_SYK)
            .queryParam("behandlingstype", BEHANDLINGSTYPE_KLAGE)
            .queryParam("behandlingstype", BEHANDLINGSTYPE_FEILUTBETALING)
            .queryParam("limit", 100)
            .queryParam("offset", offset)
            .build()
    }

    private fun OppgaveSearchCriteria.buildUri(origUriBuilder: UriBuilder, offset: Int): URI {
        logger.debug("Searchcriteria: {}", this)
        var uriBuilder = origUriBuilder
            .queryParam("statuskategori", OppgaveClient.STATUSKATEGORI_AAPEN)
            .queryParam("limit", 100)
            .queryParam("offset", offset)

        this.type?.let { uriBuilder = uriBuilder.queryParam("behandlingstype", mapType(it)) }
        this.ytelse?.let { uriBuilder = uriBuilder.queryParam("tema", mapYtelseTilTema(it)) }
        this.erTildeltSaksbehandler?.let { uriBuilder = uriBuilder.queryParam("tildeltRessurs", it) }
        this.saksbehandler?.let { uriBuilder = uriBuilder.queryParam("tilordnetRessurs", it) }
        val uri = uriBuilder.build()
        logger.info("Making searchrequest with query {}", uri.query)
        return uri
    }

    private fun mapType(type: String): String {
        //TODO
        return BEHANDLINGSTYPE_KLAGE
    }

    private fun mapYtelseTilTema(ytelse: String): String {
        //TODO
        return TEMA_SYK
    }

    @Retryable
    fun putOppgave(
        oppgaveId: Int,
        oppgave: EndreOppgave
    ): Oppgave {
        return oppgaveWebClient.put()
            .uri { uriBuilder ->
                uriBuilder.pathSegment("{id}").build(oppgaveId)
            }
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer ${stsClient.oidcToken()}")
            .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .bodyValue(oppgave)
            .retrieve()
            .bodyToMono<Oppgave>()
            .block() ?: throw OppgaveNotFoundException("Oppgave could not be put")
    }

    @Retryable
    fun getOppgave(oppgaveId: Int): Oppgave {
        return oppgaveWebClient.get()
            .uri { uriBuilder ->
                uriBuilder.pathSegment("{id}").build(oppgaveId)
            }
            .header("Authorization", "Bearer ${stsClient.oidcToken()}")
            .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
            .header("Nav-Consumer-Id", applicationName)
            .retrieve()
            .bodyToMono<Oppgave>()
            .block() ?: throw OppgaveNotFoundException("Oppgave could not be fetched")
    }

    private fun logTimingAndWebClientResponseException(function: () -> OppgaveResponse): OppgaveResponse {
        val start: Long = currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            securelogger.error(
                "Got a {} error calling Oppgave {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            if (rtex.cause is WebClientResponseException) {
                logger.debug("WebClientResponseException is wrapped in RuntimeException", rtex)
                val cause: WebClientResponseException = rtex.cause as WebClientResponseException
                securelogger.error(
                    "Got a {} error calling Oppgave {} {} with message {}",
                    cause.statusCode,
                    cause.request?.method ?: "-",
                    cause.request?.uri ?: "-",
                    cause.responseBodyAsString
                )
                throw cause
            } else {
                logger.debug("Caught runtimeexception", rtex)
                throw rtex
            }
        } finally {
            val end: Long = currentTimeMillis()
            logger.info("It took {} millis to retrieve one page of Oppgaver", (end - start))
        }
    }
}



