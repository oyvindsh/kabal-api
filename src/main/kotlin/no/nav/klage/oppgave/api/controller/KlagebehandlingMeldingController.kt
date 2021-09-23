package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.oppgave.api.mapper.MeldingMapper
import no.nav.klage.oppgave.api.view.MeldingCreated
import no.nav.klage.oppgave.api.view.MeldingInput
import no.nav.klage.oppgave.api.view.MeldingModified
import no.nav.klage.oppgave.api.view.MeldingView
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.MeldingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logKlagebehandlingMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/klagebehandlinger")
class KlagebehandlingMeldingController(
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val meldingService: MeldingService,
    private val meldingMapper: MeldingMapper,
    private val klagebehandlingService: KlagebehandlingService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Legg til ny melding til klagebehandling",
        notes = "Legger inn ny melding på en klagebehandling"
    )
    @PostMapping("/{id}/meldinger")
    @ResponseStatus(HttpStatus.CREATED)
    fun addMelding(
        @PathVariable("id") klagebehandlingId: UUID,
        @RequestBody input: MeldingInput
    ): MeldingCreated {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("addMelding", innloggetIdent, klagebehandlingId, logger)

        validateAccessToKlagebehandling(klagebehandlingId)

        return meldingMapper.toCreatedView(
            meldingService.addMelding(
                klagebehandlingId,
                innloggetIdent,
                input.text
            )
        )
    }

    @ApiOperation(
        value = "Hent alle meldinger på en klagebehandling",
        notes = "Henter alle meldinger på en klagebehandling. Sist først."
    )
    @GetMapping("/{id}/meldinger")
    fun getMeldinger(
        @PathVariable("id") klagebehandlingId: UUID,
        @RequestBody input: MeldingInput
    ): List<MeldingView> {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("getMeldinger", innloggetIdent, klagebehandlingId, logger)

        validateAccessToKlagebehandling(klagebehandlingId)

        return meldingMapper.toMeldingerView(meldingService.getMeldingerForKlagebehandling(klagebehandlingId))
    }

    @ApiOperation(
        value = "Slett melding på en klagebehandling",
        notes = "Sletter en melding på en klagebehandling"
    )
    @DeleteMapping("/{klagebehandlingId}/meldinger/{meldingId}")
    fun deleteMelding(
        @PathVariable("klagebehandlingId") klagebehandlingId: UUID,
        @PathVariable("meldingId") meldingId: UUID
    ) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("deleteMelding", innloggetIdent, klagebehandlingId, logger)

        validateAccessToKlagebehandling(klagebehandlingId)

        meldingService.deleteMelding(
            klagebehandlingId,
            innloggetIdent,
            meldingId
        )
    }

    @ApiOperation(
        value = "Endre meldingstekst på en melding i en klagebehandling",
        notes = "Endrer tekst på en melding"
    )
    @PutMapping("/{klagebehandlingId}/meldinger/{meldingId}")
    fun modifyMelding(
        @PathVariable("klagebehandlingId") klagebehandlingId: UUID,
        @PathVariable("meldingId") meldingId: UUID,
        @RequestBody input: MeldingInput
    ): MeldingModified {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        logKlagebehandlingMethodDetails("modifyMelding", innloggetIdent, klagebehandlingId, logger)

        validateAccessToKlagebehandling(klagebehandlingId)

        return meldingMapper.toModifiedView(
            meldingService.modifyMelding(
                klagebehandlingId,
                innloggetIdent,
                meldingId,
                input.text
            )
        )
    }

    private fun validateAccessToKlagebehandling(klagebehandlingId: UUID) {
        klagebehandlingService.getKlagebehandling(klagebehandlingId)
    }

}