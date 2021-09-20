package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logKlagebehandlingMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class KlagebehandlingMeldingController(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val klagebehandlingService: KlagebehandlingService,
    private val saksbehandlerService: SaksbehandlerService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/klagebehandlinger/{id}/meldinger")
    @ResponseStatus(HttpStatus.CREATED)
    fun addMelding(
        @PathVariable("id") klagebehandlingId: UUID,
        @RequestBody input: MeldingInput
    ): MeldingCreated {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("addMelding", innloggetIdent, klagebehandlingId, logger)

        val (version, created) = klagebehandlingService.addMelding(
            klagebehandlingId,
            innloggetIdent,
            input.text,
            input.klagebehandlingVersion
        )

        return MeldingCreated(version, created)
    }

    @GetMapping("/klagebehandlinger/{id}/meldinger")
    fun getMeldinger(
        @PathVariable("id") klagebehandlingId: UUID,
        @RequestBody input: MeldingInput
    ): List<MeldingView> {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("getMeldinger", innloggetIdent, klagebehandlingId, logger)

        val klagebehandling = klagebehandlingService.getKlagebehandling(klagebehandlingId)

        val names = saksbehandlerService.getNamesForSaksbehandlere(
            klagebehandling.meldinger.map { it.saksbehandlerident }.toSet()
        )

        return klagebehandling.meldinger.sorted().map { melding ->
            MeldingView(
                id = melding.id,
                text = melding.text,
                author = MeldingView.Author(
                    saksbehandlerIdent = melding.saksbehandlerident,
                    name = names[melding.saksbehandlerident] ?: "ukjent navn",
                ),
                created = melding.created,
                modified = melding.modified
            )
        }
    }

    @DeleteMapping("/klagebehandlinger/{klagebehandlingId}/meldinger/{meldingId}")
    fun deleteMelding(
        @PathVariable("klagebehandlingId") klagebehandlingId: UUID,
        @PathVariable("meldingId") meldingId: UUID,
        @RequestBody input: MeldingDeleteInput
    ): MeldingDeleted {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("deleteMelding", innloggetIdent, klagebehandlingId, logger)

        val version = klagebehandlingService.deleteMelding(
            klagebehandlingId,
            innloggetIdent,
            meldingId,
            input.klagebehandlingVersion
        )

        return MeldingDeleted(version)
    }

    @PutMapping("/klagebehandlinger/{klagebehandlingId}/meldinger/{meldingId}")
    fun modifyMelding(
        @PathVariable("klagebehandlingId") klagebehandlingId: UUID,
        @PathVariable("meldingId") meldingId: UUID,
        @RequestBody input: MeldingInput
    ): MeldingModified {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("modifyMelding", innloggetIdent, klagebehandlingId, logger)

        val (version, modified) = klagebehandlingService.modifyMelding(
            klagebehandlingId,
            innloggetIdent,
            meldingId,
            input.text,
            input.klagebehandlingVersion
        )

        return MeldingModified(version, modified)
    }

}