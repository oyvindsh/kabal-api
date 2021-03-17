package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.OversendelseService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("oversendelse")
class OversendelseController(
    private val oversendelseService: OversendelseService
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
        @ApiParam(value = "Oversendt klage")
        @Valid @RequestBody oversendtKlage: OversendtKlage
    ) {
        oversendelseService.createMottakForKlage(oversendtKlage)
    }
}
