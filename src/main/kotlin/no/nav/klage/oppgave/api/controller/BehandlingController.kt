package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.mapper.BehandlingMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.FullmektigSearchService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logBehandlingMethodDetails
import no.nav.klage.oppgave.util.logKlagebehandlingMethodDetails
import no.nav.klage.oppgave.util.logMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class BehandlingController(
    private val behandlingService: BehandlingService,
    private val behandlingMapper: BehandlingMapper,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val fullmektigSearchService: FullmektigSearchService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/klagebehandlinger/{behandlingId}/sattpaavent")
    fun setSattPaaVent(
        @Parameter(description = "Id til en behandling")
        @PathVariable("behandlingId") behandlingId: UUID,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setSattPaaVent.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val modified = behandlingService.setSattPaaVent(
            behandlingId = behandlingId,
            setNull = false,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return BehandlingEditedView(modified = modified)
    }

    @DeleteMapping("/klagebehandlinger/{behandlingId}/sattpaavent")
    fun deleteSattPaaVent(
        @Parameter(description = "Id til en behandling")
        @PathVariable("behandlingId") behandlingId: UUID,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::deleteSattPaaVent.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )
        val modified = behandlingService.setSattPaaVent(
            behandlingId = behandlingId,
            setNull = true,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return BehandlingEditedView(modified = modified)
    }

    @PostMapping("/behandlinger/{id}/fullfoer")
    fun fullfoerBehandling(
        @PathVariable("id") behandlingId: UUID
    ): BehandlingFullfoertView {
        logKlagebehandlingMethodDetails(
            ::fullfoerBehandling.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val klagebehandling = behandlingService.ferdigstillBehandling(
            behandlingId,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return behandlingMapper.mapToBehandlingFullfoertView(klagebehandling)
    }


    @PutMapping("/behandlinger/{id}/mottattklageinstans")
    fun setMottattKlageinstans(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: BehandlingDateInput
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setMottattKlageinstans.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setMottattKlageinstans(
            behandlingId = behandlingId,
            date = input.date.atStartOfDay(),
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PutMapping("/behandlinger/{id}/mottattvedtaksinstans")
    fun setMottattVedtaksinstans(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: BehandlingDateInput
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setMottattVedtaksinstans.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setMottattVedtaksinstans(
            behandlingId = behandlingId,
            date = input.date,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PutMapping("/behandlinger/{id}/sendttiltrygderetten")
    fun setSendtTilTrygderetten(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: BehandlingDateInput
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setSendtTilTrygderetten.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setSendtTilTrygderetten(
            behandlingId = behandlingId,
            date = input.date.atStartOfDay(),
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PutMapping("/behandlinger/{id}/kjennelsemottatt")
    fun setKjennelseMottatt(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: BehandlingDateInput
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setKjennelseMottatt.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setKjennelseMottatt(
            behandlingId = behandlingId,
            date = input.date.atStartOfDay(),
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PutMapping("/behandlinger/{id}/frist")
    fun setFrist(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: BehandlingDateInput
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setFrist.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setFrist(
            behandlingId = behandlingId,
            frist = input.date,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    /**
     * Valgfri validering før innsending/fullføring.
     * Gjøres uansett ved fullføring av behandlingen.
     */
    @GetMapping("/behandlinger/{id}/validate")
    fun validate(
        @PathVariable("id") behandlingId: UUID
    ): ValidationPassedResponse {
        logKlagebehandlingMethodDetails(
            ::validate.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        behandlingService.validateBehandlingBeforeFinalize(
            behandlingService.getBehandling(
                behandlingId
            )
        )
        return ValidationPassedResponse()
    }

    @PutMapping("/behandlinger/{id}/innsendingshjemler")
    fun setInnsendingshjemler(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: InnsendingshjemlerInput,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setInnsendingshjemler.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setInnsendingshjemler(
            behandlingId = behandlingId,
            hjemler = input.hjemler,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PutMapping("/behandlinger/{id}/fullmektig")
    fun setFullmektig(
        @PathVariable("id") behandlingId: UUID,
        @RequestBody input: FullmektigInput,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setFullmektig.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setFullmektig(
            behandlingId = behandlingId,
            identifikator = input.identifikator,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PostMapping("/searchfullmektig")
    fun searchFullmektig(
        @RequestBody input: SearchFullmektigInput,
    ): BehandlingDetaljerView.ProsessfullmektigView {
        logMethodDetails(
            ::searchFullmektig.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return fullmektigSearchService.searchFullmektig(input.identifikator)
    }
}