package no.nav.klage.dokument.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.dokument.clients.kabalsmarteditorapi.KabalSmartEditorApiClient
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.request.CommentInput
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response.CommentOutput
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response.DocumentOutput
import no.nav.klage.dokument.domain.dokumenterunderarbeid.PersistentDokumentId
import no.nav.klage.dokument.service.DokumentUnderArbeidService
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/smarteditor/documents")
class SmartEditorController(
    private val kabalSmartEditorApiClient: KabalSmartEditorApiClient,
    private val dokumentUnderArbeidService: DokumentUnderArbeidService

) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @ApiOperation(
        value = "Update document",
        notes = "Update document"
    )
    @PutMapping("/{documentId}")
    fun updateDocument(
        @PathVariable("documentId") documentId: UUID,
        @RequestBody jsonInput: String
    ): DocumentOutput {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                persistentDokumentId = PersistentDokumentId(documentId),
                readOnly = false
            )
        return kabalSmartEditorApiClient.updateDocument(smartEditorId, jsonInput)
    }

    @ApiOperation(
        value = "Get document",
        notes = "Get document"
    )
    @GetMapping("/{documentId}")
    fun getDocument(@PathVariable("documentId") documentId: UUID): DocumentOutput {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                persistentDokumentId = PersistentDokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.getDocument(smartEditorId)
    }

    @ApiOperation(
        value = "Create comment for a given document",
        notes = "Create comment for a given document"
    )
    @PostMapping("/{documentId}/comments")
    fun createComment(
        @PathVariable("documentId") documentId: UUID,
        @RequestBody commentInput: CommentInput
    ): CommentOutput {
        //TODO: Skal hvem som helst få kommentere?
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                persistentDokumentId = PersistentDokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.createcomment(smartEditorId, commentInput)
    }

    @ApiOperation(
        value = "Get all comments for a given document",
        notes = "Get all comments for a given document"
    )
    @GetMapping("/{documentId}/comments")
    fun getAllCommentsWithPossibleThreads(
        @PathVariable("documentId") documentId: UUID
    ): List<CommentOutput> {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                persistentDokumentId = PersistentDokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.getAllCommentsWithPossibleThreads(smartEditorId)
    }

    @ApiOperation(
        value = "Reply to a given comment",
        notes = "Reply to a given comment"
    )
    @PostMapping("/{documentId}/comments/{commentId}/replies")
    fun replyToComment(
        @PathVariable("documentId") documentId: UUID,
        @PathVariable("commentId") commentId: UUID,
        @RequestBody commentInput: CommentInput,
    ): CommentOutput {
        //TODO: Skal hvem som helst få kommentere?
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                persistentDokumentId = PersistentDokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.replyToComment(smartEditorId, commentId, commentInput)
    }

    @ApiOperation(
        value = "Get a given comment",
        notes = "Get a given comment"
    )
    @GetMapping("/{documentId}/comments/{commentId}")
    fun getCommentWithPossibleThread(
        @PathVariable("documentId") documentId: UUID,
        @PathVariable("commentId") commentId: UUID
    ): CommentOutput {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                persistentDokumentId = PersistentDokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.getCommentWithPossibleThread(smartEditorId, commentId)
    }
}