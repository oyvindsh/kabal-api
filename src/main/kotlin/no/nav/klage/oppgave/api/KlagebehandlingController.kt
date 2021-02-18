package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingController(
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}")
    fun getKlagebehandling(
        @PathVariable("id") klagebehandlingId: String
    ): KlagebehandlingView {
        logger.debug(
            "getKlagebehandling is requested by ident {} for oppgaveId {}",
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
        return klagebehandlingFacade.getKlagebehandling(klagebehandlingId.toUUIDOrException()).also {
            auditLogger.log(
                AuditLogEvent(
                    navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent(),
                    action = AuditLogEvent.Action.KLAGEBEHANDLING_VIEW,
                    decision = AuditLogEvent.Decision.ALLOW,
                    personFnr = it.foedselsnummer
                )
            )
        }
    }

    private fun String.toUUIDOrException() =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            logger.error("KlagebehandlingId could not be parsed as an UUID", e)
            throw BehandlingsidWrongFormatException("KlagebehandlingId could not be parsed as an UUID")
        }
}