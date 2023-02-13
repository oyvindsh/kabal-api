package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.MottakService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/api/internal")
class KabinApiController(
    private val klagebehandlingService: KlagebehandlingService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val mottakService: MottakService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/completedklagebehandlinger")
    fun getCompletedKlagebehandlinger(
        @RequestBody input: GetCompletedKlagebehandlingerInput
    ): List<CompletedKlagebehandling> {
        logMethodDetails(
            methodName = ::getCompletedKlagebehandlinger.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return klagebehandlingService.findCompletedKlagebehandlingerByPartIdValue(partIdValue = input.idnummer)
    }

    @PostMapping("/createanke")
    fun createAnke(
        @RequestBody input: CreateAnkeBasedOnKabinInput
    ) {
        logMethodDetails(
            methodName = ::createAnke.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        mottakService.createAnkeMottakFromKabinInput(input = input)

        //TODO: Sjekk behov for å sende Kafka-melding om ANKE_OPPRETTET, dobbeltsjekk DVH
    }
}