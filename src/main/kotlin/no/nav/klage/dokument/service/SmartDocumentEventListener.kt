package no.nav.klage.dokument.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.dokument.domain.PatchEvent
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.*

@Service
class SmartDocumentEventListener(
    private val aivenKafkaTemplate: KafkaTemplate<String, String>,
    @Value("\${SMARTDOCUMENT_EVENTS_TOPIC}")
    private val topic: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun subscribeToDocumentChanges(documentId: UUID, patchVersion: Long, emitter: SseEmitter) {
        DocumentSubscriberStore.addSubscriber(documentId, emitter)

        //initial for new subscribers
        try {
            DocumentPatchStore.getPatchEvents(documentId = documentId, patchVersion = patchVersion)
                .forEach { patchEvent ->
                    val builder = SseEmitter.event()
                        .name("patch")
                        .data(patchEvent.json)
                        .reconnectTime(200)
                        .id(patchEvent.patchVersion.toString())
                    emitter.send(builder)
                }
        } catch (e: Exception) {
            logger.error("Failed emitting patch. Removing subscriber.", e)
            emitter.completeWithError(e)
            DocumentSubscriberStore.removeSubscriber(documentId, emitter)
        }
    }

    fun subscribeToEditorChanges(documentId: UUID, editorPath: String, operationVersion: Long, emitter: SseEmitter) {
        EditorPatchSubscriberStore.addSubscriber(documentId, editorPath, emitter)

        //initial for new subscribers
        try {
            EditorPatchStore.getPatchEvents(
                documentId = documentId,
                editorPath = editorPath,
                operationVersion = operationVersion
            )
                .forEach { patchEvent ->
                    val builder = SseEmitter.event()
                        .name("operation")
                        .data(patchEvent.json)
                        .reconnectTime(200)
                        .id(patchEvent.patchVersion.toString())
                    emitter.send(builder)
                }
        } catch (e: Exception) {
            logger.error("Failed emitting editor patch. Removing subscriber.", e)
            emitter.completeWithError(e)
            EditorPatchSubscriberStore.removeSubscriber(
                documentId = documentId,
                editorPath = editorPath,
                sseEmitter = emitter
            )
        }
    }

    fun handlePatchEvent(patchEvent: PatchEvent) {
        DocumentPatchStore.addPatchEvent(patchEvent.documentId, patchEvent)
        sendSmarteditorDocumentEvent(patchEvent)
    }

    fun handleEditorPatchEvent(patchEvent: PatchEvent) {
        EditorPatchStore.addPatchEvent(
            documentId = patchEvent.documentId,
            patchEvent = patchEvent
        )
        sendSmarteditorDocumentEvent(patchEvent)
    }

    fun sendSmarteditorDocumentEvent(patchEvent: PatchEvent) {
        logger.debug("Sending to Kafka topic: {}", topic)
        runCatching {
            val result = aivenKafkaTemplate.send(
                topic,
                //TODO what to use as id?
                UUID.randomUUID().toString(),
                jacksonObjectMapper().writeValueAsString(patchEvent),
            ).get()
            logger.info("Patch event sent to Kafka")
            secureLogger.debug("Patch event for documentId ${patchEvent.documentId} sent to kafka ($result)")
        }.onFailure {
            val errorMessage =
                "Could not send patch event to Kafka. Check secure logs for more information."
            logger.error(errorMessage)
            secureLogger.error(
                "Could not send patch event to Kafka",
                it
            )
        }
    }

    @KafkaListener(
        id = "patchEventsListener",
        idIsGroup = false,
        topics = ["\${SMARTDOCUMENT_EVENTS_TOPIC}"],
        containerFactory = "smartdocumentPatchEventsKafkaListenerContainerFactory"
    )
    fun listenToPatchEvents(
        record: ConsumerRecord<String, String>,
        //ack: Acknowledgment,
        @Header(KafkaHeaders.GROUP_ID) groupId: String
    ) {
        runCatching {
            logger.debug("Reading offset ${record.offset()} from partition ${record.partition()} on kafka topic ${record.topic()} using groupId $groupId")
            val documentId = record.key()
            logger.debug("Read smartdocument patch with id $documentId")

            val patchEvent = jacksonObjectMapper().readValue(record.value(), PatchEvent::class.java)

            if (patchEvent.editorPath != null) {
                if (!EditorPatchStore.containsPatchEvent(
                        documentId = UUID.fromString(documentId),
                        patchEvent = patchEvent
                    )
                ) {
                    EditorPatchStore.addPatchEvent(documentId = UUID.fromString(documentId), patchEvent = patchEvent)
                }

                EditorPatchSubscriberStore.getSubcribers(patchEvent.documentId, patchEvent.editorPath)
                    .forEach { emitter ->
                        try {
                            val builder = SseEmitter.event()
                                .name("operation")
                                .data(patchEvent.json)
                                .reconnectTime(200)
                                .id(patchEvent.patchVersion.toString())
                            emitter.send(builder)
                        } catch (e: Exception) {
                            logger.error("Failed emitting patch. Removing subscriber.", e)
                            emitter.completeWithError(e)
                            EditorPatchSubscriberStore.removeSubscriber(
                                patchEvent.documentId,
                                patchEvent.editorPath,
                                emitter
                            )
                        }
                    }
            } else {
                if (!DocumentPatchStore.containsPatchEvent(
                        documentId = UUID.fromString(documentId),
                        patchEvent = patchEvent
                    )
                ) {
                    DocumentPatchStore.addPatchEvent(documentId = UUID.fromString(documentId), patchEvent = patchEvent)
                }

                DocumentSubscriberStore.getSubcribers(patchEvent.documentId).forEach { emitter ->
                    try {
                        val builder = SseEmitter.event()
                            .name("patch")
                            .data(patchEvent.json)
                            .reconnectTime(200)
                            .id(patchEvent.patchVersion.toString())
                        emitter.send(builder)
                    } catch (e: Exception) {
                        logger.error("Failed emitting patch. Removing subscriber.", e)
                        emitter.completeWithError(e)
                        DocumentSubscriberStore.removeSubscriber(patchEvent.documentId, emitter)
                    }
                }
            }


        }.onFailure {
            secureLogger.error("Failed to process patch record", it)
            throw RuntimeException("Could not process patch record. See more details in secure log.")
        }
    }
}

object DocumentSubscriberStore {
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

object EditorPatchSubscriberStore {
    private val store: MutableMap<Pair<UUID, String>, MutableList<SseEmitter>> = mutableMapOf()

    fun addSubscriber(documentId: UUID, editorPath: String, sseEmitter: SseEmitter) {
        if (store.containsKey(documentId to editorPath)) {
            store[documentId to editorPath]?.plusAssign(sseEmitter)
        } else {
            store[documentId to editorPath] = mutableListOf(sseEmitter)
        }
    }

    fun removeSubscriber(documentId: UUID, editorPath: String, sseEmitter: SseEmitter) {
        store[documentId to editorPath]?.remove(sseEmitter)
    }

    fun getSubcribers(documentId: UUID, editorPath: String): List<SseEmitter> {
        return store[documentId to editorPath] ?: emptyList()
    }
}

object EditorPatchStore {
    private val store: MutableMap<Pair<UUID, String>, MutableList<PatchEvent>> = mutableMapOf()

    fun addPatchEvent(documentId: UUID, patchEvent: PatchEvent) {
        if (store.containsKey(documentId to patchEvent.editorPath)) {
            store[documentId to patchEvent.editorPath]?.plusAssign(patchEvent)
        } else {
            store[documentId to patchEvent.editorPath!!] = mutableListOf(patchEvent)
        }
    }

    fun containsPatchEvent(documentId: UUID, patchEvent: PatchEvent): Boolean {
        return store[documentId to patchEvent.editorPath]?.any { it.patchVersion == patchEvent.patchVersion } ?: false
    }

    fun getPatchEvents(documentId: UUID, editorPath: String, operationVersion: Long): List<PatchEvent> {
        return if (store.containsKey(documentId to editorPath)) {
            val sortedList = store[documentId to editorPath]!!.sortedBy { it.patchVersion }
            val index = sortedList.indexOfFirst { it.patchVersion == operationVersion }
            if (index == sortedList.lastIndex) {
                return emptyList()
            }
            sortedList.subList(fromIndex = index, toIndex = sortedList.size)
        } else {
            emptyList()
        }
    }

    fun getLastOperationVersion(documentId: UUID, editorPath: String): Long {
        return if (store.containsKey(documentId to editorPath)) {
            store[documentId to editorPath]!!.maxOf { it.patchVersion }
        } else {
            0
        }
    }
}

object DocumentPatchStore {
    private val store: MutableMap<UUID, MutableList<PatchEvent>> = mutableMapOf()

    fun addPatchEvent(documentId: UUID, patchEvent: PatchEvent) {
        if (store.containsKey(documentId)) {
            store[documentId]?.plusAssign(patchEvent)
        } else {
            store[documentId] = mutableListOf(patchEvent)
        }
    }

    fun containsPatchEvent(documentId: UUID, patchEvent: PatchEvent): Boolean {
        return store[documentId]?.any { it.patchVersion == patchEvent.patchVersion } ?: false
    }

    fun getPatchEvents(documentId: UUID, patchVersion: Long): List<PatchEvent> {
        return if (store.containsKey(documentId)) {
            val sortedList = store[documentId]!!.sortedBy { it.patchVersion }
            val index = sortedList.indexOfFirst { it.patchVersion == patchVersion }
            if (index == sortedList.lastIndex) {
                return emptyList()
            }
            sortedList.subList(fromIndex = index, toIndex = sortedList.size)
        } else {
            emptyList()
        }
    }

    fun getLastPatchVersion(documentId: UUID): Long {
        return if (store.containsKey(documentId)) {
            store[documentId]!!.maxOf { it.patchVersion }
        } else {
            0
        }
    }
}