package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.exceptions.BehandlingsidWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingDetaljerController(
    private val klagebehandlingService: KlagebehandlingService,
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger,
    private val environment: Environment,
    private val saksbehandlerService: SaksbehandlerService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/klagebehandlinger/{id}/muligemedunderskrivere")
    fun getPossibleMedunderskrivere(
        @PathVariable("id") klagebehandlingId: String
    ): Medunderskrivere {
        val navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logger.debug("getPossibleMedunderskrivere is requested by $navIdent")
        val klagebehandling = klagebehandlingService.getKlagebehandling(klagebehandlingId.toUUIDOrException())
        val tema = klagebehandling.tema
        return if (environment.activeProfiles.contains("prod-gcp")) {
            saksbehandlerService.getMedunderskrivere(navIdent, klagebehandling)
        } else Medunderskrivere(
            tema.id,
            listOf(
                Medunderskriver("Z994488", "F_Z994488, E_Z994488"),
                Medunderskriver("Z994330", "F_Z994330 E_Z994330"),
                Medunderskriver("Z994861", "F_Z994861 E_Z994861"),
                Medunderskriver("Z994864", "F_Z994864 E_Z994864"),
                Medunderskriver("Z994863", "F_Z994863 E_Z994863"),
                Medunderskriver("Z994862", "F_Z994862 E_Z994862"),
            ).filter { it.ident != navIdent }
        )
    }

    @GetMapping("/klagebehandlinger/{id}/detaljer")
    fun getKlagebehandlingDetaljer(
        @PathVariable("id") klagebehandlingId: String
    ): KlagebehandlingDetaljerView {
        logMethodDetails("getKlagebehandlingDetaljer", klagebehandlingId)
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingDetaljerView(
            klagebehandlingService.getKlagebehandling(klagebehandlingId.toUUIDOrException())
        ).also {
            auditLogger.log(
                AuditLogEvent(
                    navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent(),
                    personFnr = it.sakenGjelderFoedselsnummer
                )
            )
        }
    }

    @PutMapping("/klagebehandlinger/{id}/detaljer/medunderskriverident")
    fun putMedunderskriverident(
        @PathVariable("id") klagebehandlingId: String,
        @RequestBody input: KlagebehandlingMedunderskriveridentInput
    ): SendtMedunderskriverView {
        logMethodDetails("putMedunderskriverident", klagebehandlingId)
        val klagebehandling = klagebehandlingService.setMedunderskriverident(
            klagebehandlingId.toUUIDOrException(),
            input.klagebehandlingVersjon,
            input.medunderskriverident,
            innloggetSaksbehandlerRepository.getInnloggetIdent()
        )
        return SendtMedunderskriverView(
            klagebehandling.versjon,
            klagebehandling.modified,
            klagebehandling.medunderskriver!!.tidspunkt.toLocalDate()
        )
    }

    private fun String.toUUIDOrException() =
        try {
            UUID.fromString(this)
        } catch (e: Exception) {
            logger.error("KlagebehandlingId could not be parsed as an UUID", e)
            throw BehandlingsidWrongFormatException("KlagebehandlingId could not be parsed as an UUID")
        }

    private fun logMethodDetails(methodName: String, klagebehandlingId: String) {
        logger.debug(
            "{} is requested by ident {} for klagebehandlingId {}",
            methodName,
            innloggetSaksbehandlerRepository.getInnloggetIdent(),
            klagebehandlingId
        )
    }
}