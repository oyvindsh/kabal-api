package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.VedtakService
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class VedtakController(
    private val vedtakService: VedtakService
) {

    @PostMapping("/vedtak/{vedtakId}/fullfoer")
    fun fullfoerVedtak(
        @PathVariable vedtakId: UUID
    ) {
        vedtakService.fullfoerVedtak(vedtakId)
    }

}
