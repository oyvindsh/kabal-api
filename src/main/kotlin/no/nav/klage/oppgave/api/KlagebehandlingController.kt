package no.nav.klage.oppgave.api

import brave.Tracer
import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.http.HttpRequest

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger,
    private val tracer: Tracer,
    @Value("\$spring.application.name") private val applicationName: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}")
    fun getKlagebehandling(
        @PathVariable("id") oppgaveId: Long,
        request: HttpRequest
    ): KlagebehandlingView {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug(
            "getKlagebehandling is requested by ident {} for oppgaveId {}",
            innloggetIdent,
            oppgaveId
        )
        return klagebehandlingFacade.getKlagebehandling(oppgaveId).also {
            auditLogger.log(AuditLogEvent(
                applicationName = applicationName,
                navIdent = innloggetIdent,
                requestURL = request.uri().toString(),
                requestMethod = request.method(),
                personFnr = it.foedselsnummer,
                traceId = tracer.currentSpan().context().traceIdString()
            ))
        }
    }

}