package no.nav.klage.dokument.service

import no.nav.klage.dokument.domain.PatchEvent
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@Service
class SmartDocumentEventListener() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun subscribeToDocumentChanges(documentId: UUID, emitter: SseEmitter) {
        SubscriberStore.addSubscriber(documentId, emitter)
    }

    //    @EventListener
    fun handlePatchEvent(patchEvent: PatchEvent) {
        SubscriberStore.getSubcribers(patchEvent.documentId).forEach { emitter ->
            try {
                val builder = SseEmitter.event()
                    .name("patch")
                    .data(patchEvent.json)
                    .reconnectTime(200)
                    .id("someId")
                emitter.send(builder)
            } catch (e: Exception) {
                logger.error("Failed emitting patch. Removing subscriber.", e)
                emitter.completeWithError(e)
                SubscriberStore.removeSubscriber(patchEvent.documentId, emitter)
            }
        }
    }
}

object SubscriberStore {
    private val store: MutableMap<UUID, MutableList<SseEmitter>> = mutableMapOf()

    fun addSubscriber(documentId: UUID, sseEmitter: SseEmitter) {
        if (store.containsKey(documentId)) {
            store[documentId]?.plusAssign(sseEmitter)
        } else {
            store[documentId] = mutableListOf(sseEmitter)
        }
    }

    fun removeSubscriber(documentId: UUID, sseEmitter: SseEmitter) {
        store[documentId]?.remove(sseEmitter)
    }

    fun getSubcribers(documentId: UUID): List<SseEmitter> {
        return store[documentId] ?: emptyList()
    }

}