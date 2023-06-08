package no.nav.klage.dokument.api.controller


import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import no.nav.klage.dokument.api.mapper.DokumentInputMapper
import no.nav.klage.dokument.api.mapper.DokumentMapper
import no.nav.klage.dokument.api.view.*
import no.nav.klage.dokument.domain.Event
import no.nav.klage.dokument.service.DokumentUnderArbeidService
import no.nav.klage.kodeverk.Brevmottakertype
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.oppgave.clients.events.KafkaEventClient
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import java.time.Duration
import java.util.*

@RestController
@Tag(name = "kabal-api-dokumenter")
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("/behandlinger/{behandlingId}/dokumenter")
class DokumentUnderArbeidController(
    private val dokumentUnderArbeidService: DokumentUnderArbeidService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val dokumentMapper: DokumentMapper,
    private val dokumentInputMapper: DokumentInputMapper,
    private val kafkaEventClient: KafkaEventClient,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/fil")
    fun createAndUploadHoveddokument(
        @PathVariable("behandlingId") behandlingId: UUID,
        @ModelAttribute input: FilInput
    ): DokumentView {
        logger.debug("Kall mottatt på createAndUploadHoveddokument")
        val opplastetFil = dokumentInputMapper.mapToMellomlagretDokument(
            multipartFile = input.file,
            tittel = input.file.originalFilename,
            dokumentType = DokumentType.of(input.dokumentTypeId),
        )
        return dokumentMapper.mapToDokumentView(
            dokumentUnderArbeidService.opprettOgMellomlagreNyttHoveddokument(
                behandlingId = behandlingId,
                dokumentType = DokumentType.of(input.dokumentTypeId),
                opplastetFil = opplastetFil,
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
                tittel = opplastetFil.title,
            )
        )
    }

    @PostMapping("/journalfoertedokumenter")
    fun addJournalfoerteDokumenterAsVedlegg(
        @PathVariable("behandlingId") behandlingId: UUID,
        @RequestBody input: JournalfoerteDokumenterInput
    ): JournalfoerteDokumenterResponse {
        logger.debug("Kall mottatt på addJournalfoerteDokumenterAsVedlegg")
        val (added, duplicates) = dokumentUnderArbeidService.createJournalfoerteDokumenter(
            parentId = input.parentId,
            journalfoerteDokumenter = input.journalfoerteDokumenter,
            behandlingId = behandlingId,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )

        return JournalfoerteDokumenterResponse(
            addedJournalfoerteDokumenter = added.map { dokumentMapper.mapToDokumentView(it) },
            duplicateJournalfoerteDokumenter = duplicates
        )
    }

    @PutMapping("/{dokumentId}/dokumenttype")
    fun endreDokumentType(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentId") dokumentId: UUID,
        @RequestBody input: DokumentTypeInput
    ): DokumentView {
        return dokumentMapper.mapToDokumentView(
            dokumentUnderArbeidService.updateDokumentType(
                behandlingId = behandlingId,
                dokumentId = dokumentId,
                dokumentType = DokumentType.of(input.dokumentTypeId),
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @ResponseBody
    @GetMapping("/{dokumentId}/pdf")
    fun getPdf(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentId") dokumentId: UUID,
    ): ResponseEntity<ByteArray> {
        logger.debug("Kall mottatt på getPdf for $dokumentId")
        return dokumentMapper.mapToByteArray(
            dokumentUnderArbeidService.getFysiskDokument(
                behandlingId = behandlingId,
                dokumentId = dokumentId,
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @DeleteMapping("/{dokumentId}")
    fun deleteDokument(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentId") dokumentId: UUID,
    ) {
        logger.debug("Kall mottatt på deleteDokument for $dokumentId")
        dokumentUnderArbeidService.slettDokument(
            behandlingId = behandlingId,
            dokumentId = dokumentId,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
    }

    @PutMapping("/{dokumentId}/parent")
    fun kobleEllerFrikobleVedlegg(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentId") persistentDokumentId: UUID,
        @RequestBody input: OptionalPersistentDokumentIdInput
    ): DokumentView {
        logger.debug("Kall mottatt på kobleEllerFrikobleVedlegg for $persistentDokumentId")
        try {
            val hovedDokument = if (input.dokumentId == null) {
                dokumentUnderArbeidService.frikobleVedlegg(
                    behandlingId = behandlingId,
                    dokumentId = persistentDokumentId,
                    innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
                )
            } else {
                dokumentUnderArbeidService.kobleVedlegg(
                    behandlingId = behandlingId,
                    dokumentId = input.dokumentId,
                    dokumentIdHovedDokumentSomSkalBliVedlegg = persistentDokumentId,
                    innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
                )
            }
            return dokumentMapper.mapToDokumentView(hovedDokument)
        } catch (e: Exception) {
            logger.error("Feilet under kobling av dokument $persistentDokumentId med ${input.dokumentId}", e)
            throw e
        }
    }

    @GetMapping
    fun findHovedDokumenter(
        @PathVariable("behandlingId") behandlingId: UUID,
    ): List<DokumentView> {
        return dokumentUnderArbeidService.findDokumenterNotFinished(behandlingId = behandlingId)
            .map { dokumentMapper.mapToDokumentView(it) }
    }

    @PostMapping("/{dokumentid}/ferdigstill")
    fun idempotentOpprettOgFerdigstillDokumentEnhetFraHovedDokument(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentid") dokumentId: UUID,
        @RequestBody(required = true) input: FerdigstillDokumentInput,
    ): DokumentView {
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()
        return dokumentMapper.mapToDokumentView(
            dokumentUnderArbeidService.finnOgMarkerFerdigHovedDokument(
                behandlingId = behandlingId,
                dokumentId = dokumentId,
                ident = ident,
                brevmottakertyper = input.brevmottakertypeIds?.map { Brevmottakertype.of(it) }?.toSet() ?: emptySet(),
            )
        )
    }

    @GetMapping("/{dokumentid}/validate")
    fun validateDokument(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentid") dokumentId: UUID,
    ): List<DocumentValidationResponse> {
        return dokumentUnderArbeidService.validateSmartDokument(dokumentId)
    }

    //Old event stuff. Clients should read from EventController instead, and this can be deleted.
    @GetMapping("/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun documentEvents(
        @PathVariable("behandlingId") behandlingId: String,
        @RequestParam("lastEventIdInput", required = false) lastEventIdInput: UUID?,
        request: HttpServletRequest,
    ): Flux<ServerSentEvent<String>> {
        logger.debug("Kall mottatt på documentEvents for behandlingId $behandlingId")

        //https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-async-disconnects
        val heartbeatStream: Flux<ServerSentEvent<String>> = Flux.interval(Duration.ofSeconds(10))
            .takeWhile { true }
            .map { tick -> toHeartBeatServerSentEvent(tick) }

        return kafkaEventClient.getEventPublisher()
            .mapNotNull { event -> jsonToEvent(event.data()) }
            .filter { Objects.nonNull(it) }
            .filter { it.behandlingId == behandlingId && it.name == "finished" }
            .mapNotNull { eventToServerSentEvent(it) }
            .mergeWith(heartbeatStream)
    }

    private fun toHeartBeatServerSentEvent(tick: Long): ServerSentEvent<String> {
        return eventToServerSentEvent(
            Event(
                behandlingId = "",
                id = "",
                name = "heartbeat-event-$tick",
                data = ""
            )
        )
    }

    private fun eventToServerSentEvent(event: Event): ServerSentEvent<String> {
        return ServerSentEvent.builder<String>()
            .id(event.id)
            .event(event.name)
            .data(event.data)
            .build()
    }

    private fun jsonToEvent(json: String?): Event {
        val event = jacksonObjectMapper().readValue(json, Event::class.java)
        logger.debug("Received event from Kafka: {}", event)
        return event
    }

    @PutMapping("/{dokumentid}/tittel")
    fun changeDocumentTitle(
        @PathVariable("behandlingId") behandlingId: UUID,
        @PathVariable("dokumentid") dokumentId: UUID,
        @RequestBody input: DokumentTitleInput,
    ): DokumentView {
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()
        return dokumentMapper.mapToDokumentView(
            dokumentUnderArbeidService.updateDokumentTitle(
                behandlingId = behandlingId,
                dokumentId = dokumentId,
                dokumentTitle = input.title,
                innloggetIdent = ident,
            )
        )
    }
}