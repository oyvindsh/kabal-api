package no.nav.klage.oppgave.clients

import brave.Tracer
import no.nav.klage.oppgave.domain.OppgaverQueryParams
import no.nav.klage.oppgave.domain.OppgaverQueryParams.Order
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.EndreOppgave
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.exceptions.OppgaveNotFoundException
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
    fun getOneSearchPage(oppgaveSearchCriteria: OppgaverQueryParams, saksbehandlerIdent: String?): OppgaveResponse {
        return logTimingAndWebClientResponseException("getOneSearchPage") {
            oppgaveWebClient.get()
                .uri { uriBuilder -> oppgaveSearchCriteria.buildUri(uriBuilder, saksbehandlerIdent) }
                .header("Authorization", "Bearer ${stsClient.oidcToken()}")
                .header("X-Correlation-ID", tracer.currentSpan().context().traceIdString())
                .header("Nav-Consumer-Id", applicationName)
                .retrieve()
                .bodyToMono<OppgaveResponse>()
                .block() ?: throw RuntimeException("Oppgaver could not be fetched")
        }
    }

    private fun OppgaverQueryParams.buildUri(origUriBuilder: UriBuilder, saksbehandlerIdent: String?): URI {
        logger.debug("Search criteria: {}", this)
        val uriBuilder = origUriBuilder
            .queryParam("statuskategori", STATUSKATEGORI_AAPEN)
            .queryParam("offset", offset)
            .queryParam("limit", limit)

        typer.forEach {
            uriBuilder.queryParam("behandlingstype", mapType(it))
        }
        ytelser.forEach {
            uriBuilder.queryParam("tema", it)
        }

//      Do we need this? ->  uriBuilder.queryParam("tildeltRessurs", true|false)
        saksbehandlerIdent?.let {
            uriBuilder.queryParam("tilordnetRessurs", saksbehandlerIdent)
        }

        //FRIST is default in oppgave-api.
//        uriBuilder.queryParam("sorteringsfelt", orderBy ?: "frist")
        uriBuilder.queryParam("sorteringsrekkefolge", order ?: Order.ASC)

        if (hjemler.isNotEmpty()) {
            uriBuilder.queryParam("metadatanokkel", HJEMMEL)
            hjemler.forEach {
                uriBuilder.queryParam("metadataverdi", it)
            }
        }

        val uri = uriBuilder.build()
        logger.info("Making search request with query {}", uri.query)
        return uri
    }

    private fun mapType(type: String): String {
        return when(type) {
            "klage" -> BEHANDLINGSTYPE_KLAGE
            else -> {
                logger.warn("invalid type: {}", type)
                throw RuntimeException("Invalid type: $type")
            }
        }
    }

    @Retryable
    fun putOppgave(
        oppgaveId: Long,
        oppgave: EndreOppgave
    ): Oppgave {
        return logTimingAndWebClientResponseException("putOppgave") {
            oppgaveWebClient.put()
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
    }

    @Retryable
    fun getOppgave(oppgaveId: Long): Oppgave {
        return logTimingAndWebClientResponseException("getOppgave") {
            oppgaveWebClient.get()
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
    }

    private fun <T> logTimingAndWebClientResponseException(methodName: String, function: () -> T): T {
        val start: Long = currentTimeMillis()
        try {
            return function.invoke()
        } catch (ex: WebClientResponseException) {
            logger.warn("Caught WebClientResponseException, see securelogs for details")
            securelogger.error(
                "Got a {} error calling Oppgave {} {} with message {}",
                ex.statusCode,
                ex.request?.method ?: "-",
                ex.request?.uri ?: "-",
                ex.responseBodyAsString
            )
            throw ex
        } catch (rtex: RuntimeException) {
            logger.warn("Caught RuntimeException", rtex)
            throw rtex
        } finally {
            val end: Long = currentTimeMillis()
            logger.info("Method {} took {} millis", methodName, (end - start))
        }
    }
}



