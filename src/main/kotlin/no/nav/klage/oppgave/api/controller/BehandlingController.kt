package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.oppgave.api.mapper.BehandlingMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Medunderskrivere
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Saksbehandlere
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.klage.SattPaaVent
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logBehandlingMethodDetails
import no.nav.klage.oppgave.util.logKlagebehandlingMethodDetails
import no.nav.klage.oppgave.util.logMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView
import java.time.LocalDate
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping(value = ["/klagebehandlinger", "/behandlinger"])
class BehandlingController(
    private val behandlingService: BehandlingService,
    private val behandlingMapper: BehandlingMapper,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val saksbehandlerService: SaksbehandlerService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/{behandlingId}/sattpaavent")
    fun setSattPaaVent(
        @Parameter(description = "Id til en behandling")
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: SattPaaVentInput?
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setSattPaaVent.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val sattPaaVent = if (input == null) {
            SattPaaVent(
                from = LocalDate.now(),
                to = LocalDate.now().plusWeeks(4),
                reason = "Satt på vent"

            )
        } else {
            SattPaaVent(
                from = LocalDate.now(),
                to = input.to,
                reason = input.reason
            )
        }

        val modified = behandlingService.setSattPaaVent(
            behandlingId = behandlingId,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            sattPaaVent = sattPaaVent,
        )
        return BehandlingEditedView(modified = modified)
    }

    @DeleteMapping("/{behandlingId}/sattpaavent")
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
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            sattPaaVent = null
        )
        return BehandlingEditedView(modified = modified)
    }

    @PostMapping("/{behandlingId}/fullfoer")
    fun fullfoerBehandling(
        @PathVariable("behandlingId") behandlingId: UUID
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


    @PutMapping("/{behandlingId}/mottattklageinstans")
    fun setMottattKlageinstans(
        @PathVariable("behandlingId") behandlingId: UUID,
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

    @PutMapping("/{behandlingId}/mottattvedtaksinstans")
    fun setMottattVedtaksinstans(
        @PathVariable("behandlingId") behandlingId: UUID,
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

    @PutMapping("/{behandlingId}/sendttiltrygderetten")
    fun setSendtTilTrygderetten(
        @PathVariable("behandlingId") behandlingId: UUID,
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

    @PutMapping("/{behandlingId}/kjennelsemottatt")
    fun setKjennelseMottatt(
        @PathVariable("behandlingId") behandlingId: UUID,
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

    @PutMapping("/{behandlingId}/frist")
    fun setFrist(
        @PathVariable("behandlingId") behandlingId: UUID,
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
    @GetMapping("/{behandlingId}/validate")
    fun validate(
        @PathVariable("behandlingId") behandlingId: UUID
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

    @PutMapping("/{behandlingId}/innsendingshjemler")
    fun setInnsendingshjemler(
        @PathVariable("behandlingId") behandlingId: UUID,
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
            hjemler = input.hjemmelIdList ?: input.hjemler ?: emptyList(),
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @PutMapping("/{behandlingId}/fullmektig")
    fun setFullmektig(
        @PathVariable("behandlingId") behandlingId: UUID,
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

    @PutMapping("/{behandlingId}/klager")
    fun setKlager(
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: KlagerInput,
    ): BehandlingEditedView {
        logBehandlingMethodDetails(
            ::setKlager.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            behandlingId,
            logger
        )

        val modified = behandlingService.setKlager(
            behandlingId = behandlingId,
            identifikator = input.identifikator,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return BehandlingEditedView(modified = modified)
    }

    @GetMapping("/{behandlingId}/potentialsaksbehandlere")
    fun getPotentialSaksbehandlere(
        @PathVariable("behandlingId") behandlingId: UUID,
    ): Saksbehandlere {
        logMethodDetails(
            ::getPotentialSaksbehandlere.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return behandlingService.getPotentialSaksbehandlereForBehandling(behandlingId = behandlingId)
    }

    @GetMapping("/{behandlingId}/potentialmedunderskrivere")
    fun getPotentialMedunderskrivere(
        @PathVariable("behandlingId") behandlingId: UUID,
    ): Medunderskrivere {
        logMethodDetails(
            ::getPotentialMedunderskrivere.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return behandlingService.getPotentialMedunderskrivereForBehandling(behandlingId = behandlingId)
    }

    @GetMapping("/{behandlingId}/sakengjelder")
    fun getSakenGjelder(
        @PathVariable("behandlingId") behandlingId: UUID,
    ): BehandlingDetaljerView.SakenGjelderView {
        logMethodDetails(
            ::getSakenGjelder.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return behandlingMapper.getSakenGjelderView(
            behandlingService.getBehandling(behandlingId).sakenGjelder
        )
    }

    @GetMapping("/{behandlingId}/ainntekt")
    fun getAInntektUrl(
        @PathVariable("behandlingId") behandlingId: UUID,
    ): ModelAndView {
        logMethodDetails(
            ::getAInntektUrl.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return ModelAndView(/* viewName = */ "redirect:" + behandlingService.getAInntektUrl(behandlingId))
    }

    @GetMapping("/{behandlingId}/aaregister")
    fun getAARegisterUrl(
        @PathVariable("behandlingId") behandlingId: UUID,
    ): ModelAndView {
        logMethodDetails(
            ::getAARegisterUrl.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        return ModelAndView(/* viewName = */ "redirect:" + behandlingService.getAARegisterUrl(behandlingId))
    }

    @PostMapping("/{behandlingId}/feilregistrer")
    fun setBehandlingFeilregistrert(
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: FeilregistreringInput,
    ): FeilregistreringResponse {
        logMethodDetails(
            ::setBehandlingFeilregistrert.name,
            innloggetSaksbehandlerService.getInnloggetIdent(),
            logger
        )

        val modifiedBehandling = behandlingService.feilregistrer(
            behandlingId = behandlingId,
            reason = input.reason,
            fagsystem = Fagsystem.KABAL,
        )

        return FeilregistreringResponse(
            feilregistrering = BehandlingDetaljerView.FeilregistreringView(
                feilregistrertAv = SaksbehandlerView(
                    navIdent = modifiedBehandling.feilregistrering!!.navIdent,
                    navn = saksbehandlerService.getNameForIdent(modifiedBehandling.feilregistrering!!.navIdent)
                ),
                registered = modifiedBehandling.feilregistrering!!.registered,
                reason = modifiedBehandling.feilregistrering!!.reason,
                fagsystemId = modifiedBehandling.feilregistrering!!.fagsystem.id
            ),
            modified = modifiedBehandling.modified,
        )
    }
}