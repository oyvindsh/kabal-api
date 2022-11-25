package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.chrono.ChronoLocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
class BehandlingAssignmentController(
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val saksbehandlerService: SaksbehandlerService,
    private val behandlingService: BehandlingService,
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO remove when FE migrated to new endpoint without navident
    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandlerOld(
        @Parameter(description = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): TildelingEditedView {
        logger.debug("assignSaksbehandlerOld is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = saksbehandlertildeling.navIdent,
            enhetId = saksbehandlerService.getEnhetForSaksbehandler(saksbehandlertildeling.navIdent).enhetId,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(behandling)
    }

    @PostMapping("/behandlinger/{id}/saksbehandlertildeling")
    fun assignSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID,
        @RequestBody saksbehandlertildeling: Saksbehandlertildeling
    ): TildelingEditedView {
        logger.debug("assignSaksbehandler is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
            behandlingId = behandlingId,
            tildeltSaksbehandlerIdent = saksbehandlertildeling.navIdent,
            enhetId = saksbehandlerService.getEnhetForSaksbehandler(saksbehandlertildeling.navIdent).enhetId,
            utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(behandling)
    }

    //TODO remove when FE migrated to new endpoint without navident
    @PostMapping("/ansatte/{navIdent}/klagebehandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandlerOld(
        @Parameter(description = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandlerOld is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
            behandlingId,
            null,
            null,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(behandling)
    }

    @PostMapping("/behandlinger/{id}/saksbehandlerfradeling")
    fun unassignSaksbehandler(
        @Parameter(description = "Id til en behandling")
        @PathVariable("id") behandlingId: UUID
    ): TildelingEditedView {
        logger.debug("unassignSaksbehandler is requested for behandling: {}", behandlingId)
        val behandling = behandlingService.assignBehandling(
            behandlingId,
            null,
            null,
            innloggetSaksbehandlerService.getInnloggetIdent()
        )
        return tildelingEditedView(behandling)
    }

    private fun tildelingEditedView(behandling: Behandling): TildelingEditedView {
        val fnr = behandling.sakenGjelder.partId.value
        val personInfo = pdlFacade.getPersonInfo(fnr)

        val erFortrolig = personInfo.harBeskyttelsesbehovFortrolig()
        val erStrengtFortrolig = personInfo.harBeskyttelsesbehovStrengtFortrolig()
        val erEgenAnsatt = egenAnsattService.erEgenAnsatt(fnr)

        val tildelingEditedView = TildelingEditedView(
            id = behandling.id.toString(),
            person = PersonView(
                fnr = fnr,
                navn = personInfo.sammensattNavn,
                sivilstand = null
            ),
            type = behandling.type.id,
            ytelse = behandling.ytelse.id,
            tema = behandling.ytelse.toTema().id,
            hjemmel = behandling.hjemler.firstOrNull()?.id,
            frist = behandling.frist,
            mottatt = behandling.mottattKlageinstans.toLocalDate(),
            erMedunderskriver = behandling.medunderskriver?.saksbehandlerident == innloggetSaksbehandlerService.getInnloggetIdent(),
            harMedunderskriver = behandling.medunderskriver != null,
            medunderskriverident = behandling.medunderskriver?.saksbehandlerident,
            medunderskriverFlyt = behandling.medunderskriverFlyt,
            utfall = null,
            avsluttetAvSaksbehandlerDate = null,
            isAvsluttetAvSaksbehandler = false,
            erTildelt = behandling.tildeling != null,
            tildeltSaksbehandlerident = behandling.tildeling?.saksbehandlerident,
            tildeltSaksbehandlerNavn = if (behandling.tildeling != null) {
                saksbehandlerService.getNameForIdent(behandling.tildeling!!.saksbehandlerident!!)
            } else null,
            saksbehandlerHarTilgang = true,
            egenAnsatt = erEgenAnsatt,
            fortrolig = erFortrolig,
            strengtFortrolig = erStrengtFortrolig,
            ageKA = behandling.mottattKlageinstans.toAgeInDays(),
            access = AccessView.ASSIGN,
            sattPaaVent = behandling.toSattPaaVent(),
        )
        return tildelingEditedView
    }

    private fun LocalDateTime.toAgeInDays() = ChronoUnit.DAYS.between(this.toLocalDate(), LocalDate.now()).toInt()

    private fun Behandling.toSattPaaVent(): Venteperiode? {
        return if (sattPaaVent != null) {
            val expires = sattPaaVent!!.plusWeeks(4)
            Venteperiode(
                from = sattPaaVent!!.toLocalDate(),
                to = expires.toLocalDate(),
                isExpired = expires.isBefore(ChronoLocalDateTime.from(LocalDateTime.now()))
            )
        } else null
    }
}