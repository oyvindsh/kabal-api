package no.nav.klage.oppgave.api

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.DokumentKnytning
import no.nav.klage.oppgave.api.view.DokumentReferanserResponse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
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

    @ApiOperation(
        value = "Hent dokumenter knyttet til en klagebehandling",
        notes = "Henter dokumentene som saksbehandler har markert at skal knyttes til klagebehandlingen."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/dokumenter", produces = ["application/json"])
    fun fetchConnectedDokumenter(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String
    ): DokumenterResponse {
        val klagebehandlingId = UUID.fromString(behandlingsid)
        return dokumentService.fetchJournalposterConnectedToKlagebehandling(klagebehandlingId)
    }

    @ApiOperation(
        value = "Hent IDene til dokumentene knyttet til en klagebehandling",
        notes = "Henter IDene til dokumentene som saksbehandler har markert at skal knyttes til klagebehandlingen."
    )
    @GetMapping("/klagebehandlinger/{behandlingsid}/tilknyttededokumenter", produces = ["application/json"])
    fun fetchConnectedDokumentIder(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String
    ): DokumentReferanserResponse {
        val klagebehandlingId = UUID.fromString(behandlingsid)
        return DokumentReferanserResponse(
            dokumentService.fetchJournalpostIderConnectedToKlagebehandling(
                klagebehandlingId
            )
        )
    }

    @ApiOperation(
        value = "Fjerner et dokument fra en klagebehandling",
        notes = "Sletter knytningen mellom en journalpost fra SAF og klagebehandlingen den har vært knyttet til."
    )
    @DeleteMapping("/klagebehandlinger/{behandlingsid}/dokumenter/{journalpostId}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deconnectDokument(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @PathVariable journalpostId: String
    ) {
        val klagebehandlingId = UUID.fromString(behandlingsid)
        dokumentService.deconnectJournalpostFromKlagebehandling(klagebehandlingId, journalpostId)
    }

    @ApiOperation(
        value = "Knytter et dokument til en klagebehandling",
        notes = "Knytter en journalpost fra SAF til klagebehandlingen."
    )
    @PostMapping("/klagebehandlinger/{behandlingsid}/dokumenter", produces = ["application/json"])
    @ResponseStatus(HttpStatus.CREATED)
    fun connectDokument(
        @ApiParam(value = "Id til klagebehandlingen i vårt system")
        @PathVariable behandlingsid: String,
        @RequestBody dokumentKnytning: DokumentKnytning
    ) {
        val klagebehandlingId = UUID.fromString(behandlingsid)
        dokumentService.connectJournalpostToKlagebehandling(klagebehandlingId, dokumentKnytning.journalpostId)
    }
}