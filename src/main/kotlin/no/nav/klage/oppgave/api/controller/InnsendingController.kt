package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.InnsendtKlage
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.InnsendingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.ws.rs.core.Response

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("innsending")
class InnsendingController(
    private val innsendingService: InnsendingService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Send inn klage til klageinstans",
        notes = "Endepunkt for å registrere en klage/anke som skal behandles av klageinstans"
    )
    @PostMapping("/klage")
    fun sendInnKlage(
        @ApiParam(value = "Innsendt klage")
        @RequestBody innsendtKlage: InnsendtKlage
    ): Response {
        val resultat = innsendingService.validerInnsending(innsendtKlage)
        if (resultat != null) {
            return Response.status(400).entity(resultat).build()
        }
        innsendingService.createMottakForKlage(innsendtKlage)
        return Response.ok().build()
    }
}
