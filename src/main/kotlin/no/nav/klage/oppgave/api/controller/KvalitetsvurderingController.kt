package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.api.view.KvalitetsvurderingManuellInput
import no.nav.klage.oppgave.api.view.KvalitetsvurderingResponse
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class KvalitetsvurderingController(
    val klagebehandlingService: KlagebehandlingService
) {

    @PostMapping("/kvalitetsvurdering/manuell")
    fun createKvalitetsvurderingFromScratch(
        @RequestBody input: KvalitetsvurderingManuellInput
    ): KvalitetsvurderingResponse =
        KvalitetsvurderingResponse(
            klagebehandlingService.createKlagebehandlingFromKvalitetsvurdering(input)
        )

}
