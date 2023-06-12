package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.dokument.api.view.JournalfoertDokumentReference
import no.nav.klage.oppgave.api.view.ReferenceToMergedDocumentsResponse
import no.nav.klage.oppgave.api.view.UpdateDocumentTitleView
import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.DokumentService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.apache.commons.io.IOUtils
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import kotlin.io.path.inputStream

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/journalposter")
class JournalpostController(
    private val kabalDocumentClient: KabalDocumentGateway,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val dokumentService: DokumentService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Operation(
        summary = "Oppdaterer filnavn i dokumentarkivet",
        description = "Oppdaterer filnavn i dokumentarkivet"
    )
    @PutMapping("/{journalpostId}/dokumenter/{dokumentInfoId}/tittel")
    fun updateTitle(
        @Parameter(description = "Id til journalpost")
        @PathVariable journalpostId: String,
        @Parameter(description = "Id til dokumentInfo")
        @PathVariable dokumentInfoId: String,
        @Parameter(description = "Ny tittel til dokumentet")
        @RequestBody input: UpdateDocumentTitleView
    ): UpdateDocumentTitleView {
        logMethodDetails(
            methodName = ::updateTitle.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger,
        )
        kabalDocumentClient.updateDocumentTitle(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId,
            title = input.tittel
        )

        return input
    }

    @Operation(
        summary = "Henter fil fra dokumentarkivet",
        description = "Henter fil fra dokumentarkivet som pdf gitt at saksbehandler har tilgang"
    )
    @ResponseBody
    @GetMapping("/{journalpostId}/dokumenter/{dokumentInfoId}/pdf")
    fun getArkivertDokumentPDF(
        @Parameter(description = "Id til journalpost")
        @PathVariable journalpostId: String,
        @Parameter(description = "Id til dokumentInfo")
        @PathVariable dokumentInfoId: String

    ): ResponseEntity<ByteArray> {
        logMethodDetails(
            methodName = ::getArkivertDokumentPDF.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger,
        )

        val arkivertDokument = dokumentService.getArkivertDokument(
            journalpostId = journalpostId,
            dokumentInfoId = dokumentInfoId
        )

        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = arkivertDokument.contentType
        responseHeaders.add("Content-Disposition", "inline")
        return ResponseEntity(
            arkivertDokument.bytes,
            responseHeaders,
            HttpStatus.OK
        )
    }

    @PostMapping("/mergedocuments")
    fun setDocumentsToMerge(
        @RequestBody documents: List<JournalfoertDokumentReference>
    ): ReferenceToMergedDocumentsResponse {
        return ReferenceToMergedDocumentsResponse(reference = dokumentService.storeDocumentsForMerging(documents))
    }

    @GetMapping("/mergedocuments/{referenceId}")
    fun getMergedDocuments(
        @PathVariable referenceId: UUID
    ): ResponseEntity<Resource> {
        val pathToMergedDocument = dokumentService.mergeDocuments(referenceId)
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=mergedfile.pdf")

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .contentLength(pathToMergedDocument.toFile().length())
            .body(InputStreamResource(pathToMergedDocument.inputStream()))
    }

    @GetMapping("/mergedocuments_bytearray/{referenceId}")
    fun getMergedDocumentsByteArray(
        @PathVariable referenceId: UUID
    ): ResponseEntity<ByteArray> {
        val pathToMergedDocument = dokumentService.mergeDocuments(referenceId)
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=mergedfile.pdf")

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .contentLength(pathToMergedDocument.toFile().length())
            .body(IOUtils.toByteArray(pathToMergedDocument.inputStream()))
    }

    @GetMapping("/mergedocuments_bytearray_resource/{referenceId}")
    fun getMergedDocumentsByteArrayResource(
        @PathVariable referenceId: UUID
    ): ResponseEntity<Resource> {
        val pathToMergedDocument = dokumentService.mergeDocuments(referenceId)
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=mergedfile.pdf")

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .contentLength(pathToMergedDocument.toFile().length())
            .body(ByteArrayResource(IOUtils.toByteArray(pathToMergedDocument.inputStream())))
    }

    @GetMapping("/mergedocuments_bytearray_inmem/{referenceId}")
    fun getMergedDocumentsByteArrayInMem(
        @PathVariable referenceId: UUID
    ): ResponseEntity<ByteArray> {
        val byteArray = dokumentService.mergeDocumentsInMem(referenceId)
        val responseHeaders = HttpHeaders()
        responseHeaders.contentType = MediaType.APPLICATION_PDF
        responseHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=mergedfile.pdf")

        return ResponseEntity.ok()
            .headers(responseHeaders)
            .contentLength(byteArray.size.toLong())
            .body(byteArray)
    }
}