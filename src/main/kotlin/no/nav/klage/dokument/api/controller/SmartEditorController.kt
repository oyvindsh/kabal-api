package no.nav.klage.dokument.api.controller

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import no.nav.klage.dokument.clients.kabalsmarteditorapi.KabalSmartEditorApiClient
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.request.CommentInput
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response.CommentOutput
import no.nav.klage.dokument.clients.kabalsmarteditorapi.model.response.DocumentOutput
import no.nav.klage.dokument.domain.PatchEvent
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentId
import no.nav.klage.dokument.service.DocumentPatchStore
import no.nav.klage.dokument.service.DokumentUnderArbeidService
import no.nav.klage.dokument.service.SmartDocumentEventListener
import no.nav.klage.dokument.util.JsonPatch
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.Duration
import java.util.*
import javax.servlet.http.HttpServletRequest


@RestController
@Api(tags = ["kabal-api-dokumenter"])
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/behandlinger/{behandlingId}/dokumenter/smarteditor/{dokumentId}")
class SmartEditorController(
    private val kabalSmartEditorApiClient: KabalSmartEditorApiClient,
    private val dokumentUnderArbeidService: DokumentUnderArbeidService,
    private val smartDocumentEventListener: SmartDocumentEventListener,

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
    @PutMapping
    fun updateDocument(
        @PathVariable("dokumentId") documentId: UUID,
        @RequestBody jsonInput: String
    ): DocumentOutput {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = false
            )
        return kabalSmartEditorApiClient.updateDocument(smartEditorId, jsonInput)
    }

    @ApiOperation(
        value = "Get document",
        notes = "Get document"
    )
    @GetMapping
    fun getDocument(@PathVariable("dokumentId") documentId: UUID): DocumentOutput {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = true
            )
        val document = kabalSmartEditorApiClient.getDocument(smartEditorId)
        document.patchVersion = DocumentPatchStore.getLastPatchVersion(documentId)
        return document
    }

    @ApiOperation(
        value = "Create comment for a given document",
        notes = "Create comment for a given document"
    )
    @PostMapping("/comments")
    fun createComment(
        @PathVariable("dokumentId") documentId: UUID,
        @RequestBody commentInput: CommentInput
    ): CommentOutput {
        //TODO: Skal hvem som helst få kommentere?
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.createcomment(smartEditorId, commentInput)
    }

    @ApiOperation(
        value = "Get all comments for a given document",
        notes = "Get all comments for a given document"
    )
    @GetMapping("/comments")
    fun getAllCommentsWithPossibleThreads(
        @PathVariable("dokumentId") documentId: UUID
    ): List<CommentOutput> {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.getAllCommentsWithPossibleThreads(smartEditorId)
    }

    @ApiOperation(
        value = "Reply to a given comment",
        notes = "Reply to a given comment"
    )
    @PostMapping("/comments/{commentId}/replies")
    fun replyToComment(
        @PathVariable("dokumentId") documentId: UUID,
        @PathVariable("commentId") commentId: UUID,
        @RequestBody commentInput: CommentInput,
    ): CommentOutput {
        //TODO: Skal hvem som helst få kommentere?
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.replyToComment(smartEditorId, commentId, commentInput)
    }

    @ApiOperation(
        value = "Get a given comment",
        notes = "Get a given comment"
    )
    @GetMapping("/comments/{commentId}")
    fun getCommentWithPossibleThread(
        @PathVariable("dokumentId") documentId: UUID,
        @PathVariable("commentId") commentId: UUID
    ): CommentOutput {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = true
            )
        return kabalSmartEditorApiClient.getCommentWithPossibleThread(smartEditorId, commentId)
    }

    @GetMapping("/events")
    fun documentEvents(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentId") documentId: UUID,
        @RequestParam("lastEventId", required = false) lastEventIdInput: Long?,
        request: HttpServletRequest,
    ): SseEmitter {
        logger.debug("/events")

        val emitter = SseEmitter(Duration.ofHours(20).toMillis())

        val initial = SseEmitter.event()
            .reconnectTime(200)
        emitter.send(initial)

        //Try header first
        val lastEventIdHeaderName = "last-event-id"
        val lastEventId = if (request.getHeader(lastEventIdHeaderName) != null) {
            request.getHeader(lastEventIdHeaderName).toLong()
        } else {
            lastEventIdInput ?: DocumentPatchStore.getLastPatchVersion(documentId)
        }
        //Subscribe to changes in this document
        smartDocumentEventListener.subscribeToDocumentChanges(
            documentId = documentId,
            patchVersion = lastEventId,
            emitter = emitter,
        )

        return emitter
    }

    @ApiOperation(
        value = "Patch document",
        notes = "Patch document"
    )
    @PatchMapping
    fun patchDocument(
        @PathVariable("dokumentId") documentId: UUID,
        @RequestBody jsonInput: String
    ): Map<String, Long>  {
        val smartEditorId =
            dokumentUnderArbeidService.getSmartEditorId(
                dokumentId = DokumentId(documentId),
                readOnly = false
            )

        val smartDocument = kabalSmartEditorApiClient.getDocument(smartEditorId)

        val rootNode = jacksonObjectMapper().readTree(jsonInput)
        val result = JsonPatch.apply(rootNode["patch"] as ArrayNode, jacksonObjectMapper().readTree(smartDocument.json))

        kabalSmartEditorApiClient.updateDocument(smartEditorId, result.toString())

        val patchEvent = PatchEvent(
            documentId = documentId,
            json = jsonInput,
            patchVersion = DocumentPatchStore.getLastPatchVersion(documentId) + 1
        )
        smartDocumentEventListener.handlePatchEvent(
            patchEvent
        )

        return mapOf("patchVersion" to patchEvent.patchVersion)
    }
}