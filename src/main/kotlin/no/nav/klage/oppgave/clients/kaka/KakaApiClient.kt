package no.nav.klage.oppgave.clients.kaka

import brave.Tracer
import no.nav.klage.oppgave.clients.kaka.model.request.SaksdataInput
import no.nav.klage.oppgave.clients.kaka.model.response.KakaOutput
import no.nav.klage.oppgave.clients.kaka.model.response.ValidationErrors
import no.nav.klage.oppgave.util.TokenUtil
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.util.*

@Component
class KakaApiClient(
    private val kakaApiWebClient: WebClient,
    private val tokenUtil: TokenUtil,
    private val tracer: Tracer
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun createKvalitetsvurdering(kvalitetsvurderingVersion: Int): KakaOutput {
        logger.debug("Creating kvalitetsvurdering i kaka")
        return kakaApiWebClient.post()
            .uri { it.path("/kabal/kvalitetsvurderinger/v$kvalitetsvurderingVersion").build() }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getAppAccessTokenWithKakaApiScope()}"
            )
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .bodyToMono<KakaOutput>()
            .block() ?: throw RuntimeException("Kvalitetsvurdering could not be created")
    }

    fun finalizeBehandling(saksdataInput: SaksdataInput, kvalitetsvurderingVersion: Int) {
        kakaApiWebClient.post()
            .uri { it.path("/kabal/saksdata/v$kvalitetsvurderingVersion").build() }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getAppAccessTokenWithKakaApiScope()}"
            )
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(saksdataInput)
            .retrieve()
            .bodyToMono<Void>()
            .block() ?: throw RuntimeException("Saksdata could not be created")
    }

    fun getValidationErrors(
        kvalitetsvurderingId: UUID,
        ytelseId: String,
        typeId: String,
        kvalitetsvurderingVersion: Int
    ): ValidationErrors {
        logger.debug("Getting validation errors from kaka-api")
        return kakaApiWebClient.get()
            .uri {
                it.path("/kabal/kvalitetsvurderinger/v$kvalitetsvurderingVersion/{kvalitetsvurderingId}/validationerrors")
                    .queryParam("ytelseId", ytelseId)
                    .queryParam("typeId", typeId)
                    .build(kvalitetsvurderingId)
            }
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer ${tokenUtil.getSaksbehandlerAccessTokenWithKakaApiScope()}"
            )
            .header("Nav-Call-Id", tracer.currentSpan().context().traceIdString())
            .retrieve()
            .bodyToMono<ValidationErrors>()
            .block() ?: throw RuntimeException("Validation errors could not be retrieved")
    }
}