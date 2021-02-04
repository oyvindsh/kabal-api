package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Api(tags = ["klage-oppgave-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
class DokumentController(
    private val dokumentService: DokumentService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent dokumenter for en klagebehandling",
        notes = "Henter alle dokumenter om en person som saksbehandler har tilgang til."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/alledokumenter", produces = ["application/json"])
    fun fetchDokumenter(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestParam(required = false, name = "antall", defaultValue = "10") pageSize: Int,
        @RequestParam(required = false, name = "forrigeSide") previousPageRef: String? = null
    ): DokumenterResponse {
        val klagebehandlingId = UUID.fromString(behandlingsid)
        return dokumentService.fetchDokumentlisteForKlagebehandling(klagebehandlingId, pageSize, previousPageRef)
    }

}