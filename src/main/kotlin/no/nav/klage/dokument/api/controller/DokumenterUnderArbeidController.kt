package no.nav.klage.dokument.api.controller


import io.swagger.annotations.Api
import no.nav.klage.dokument.api.mapper.DokumentInputMapper
import no.nav.klage.dokument.api.mapper.DokumentMapper
import no.nav.klage.dokument.api.view.*
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentType
import no.nav.klage.dokument.domain.dokumenterunderarbeid.PersistentDokumentId
import no.nav.klage.dokument.service.DokumentService
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = ["kabal-document"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
@RequestMapping("/dokumenter")
class DokumentUnderArbeidController(
    private val dokumentService: DokumentService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerRepository,
    private val dokumentMapper: DokumentMapper,
    private val dokumenInputMapper: DokumentInputMapper,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/hoveddokumenter/fil")
    fun createAndUploadHoveddokument(
        @RequestBody body: HovedDokumentInput,
        @ModelAttribute input: FilInput
    ): HovedDokumentView {
        logger.debug("Kall mottatt på createAndUploadHoveddokument")
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.opprettOgMellomlagreNyttHoveddokument(
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
                dokumentType = DokumentType.BREV,
                behandlingId = body.eksternReferanse,
                opplastetFil = dokumenInputMapper.mapToMellomlagretDokument(input.file),
                json = null,
            )
        )
    }

    @PostMapping("/hoveddokumenter/smart")
    fun createSmartHoveddokument(
        @RequestBody body: SmartHovedDokumentInput,
    ): HovedDokumentView {
        logger.debug("Kall mottatt på createSmartHoveddokument")
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.opprettOgMellomlagreNyttHoveddokument(
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
                dokumentType = DokumentType.BREV,
                behandlingId = body.eksternReferanse,
                opplastetFil = null,
                json = body.json,
            )
        )
    }

    @PutMapping("/{dokumentId}/dokumenttype")
    fun endreDokumentType(
        @PathVariable("dokumentId") dokumentId: UUID,
        @RequestBody input: DokumentTypeInput
    ): HovedDokumentView {
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.updateDokumentType(
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
                persistentDokumentId = PersistentDokumentId(dokumentId),
                dokumentType = DokumentType.of(input.dokumentTypeId)
            )
        )
    }

    //TODO: Har hoppet over endepunkter for å oppdatere/erstatte dokumentet

    @ResponseBody
    @GetMapping("/{dokumentId}/pdf")
    fun getPdf(
        @PathVariable("dokumentId") dokumentId: UUID,
    ): ResponseEntity<ByteArray> {
        logger.debug("Kall mottatt på getPdf for $dokumentId")
        return dokumentMapper.mapToByteArray(
            dokumentService.hentMellomlagretDokument(
                persistentDokumentId = PersistentDokumentId(dokumentId),
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @DeleteMapping("/{dokumentId}")
    fun deleteDokument(
        @PathVariable("dokumentId") dokumentId: UUID,
    ) {
        logger.debug("Kall mottatt på deleteDokument for $dokumentId")
        dokumentService.slettDokument(
            persistentDokumentId = PersistentDokumentId(dokumentId),
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        )
    }

    @PostMapping("/{dokumentId}/vedlegg")
    fun kobleVedlegg(
        @PathVariable("dokumentId") persistentDokumentId: UUID,
        @RequestBody input: PersistentDokumentIdInput
    ): HovedDokumentView {
        logger.debug("Kall mottatt på kobleVedlegg for $persistentDokumentId")
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.kobleVedlegg(
                persistentDokumentId = PersistentDokumentId(persistentDokumentId),
                persistentDokumentIdHovedDokumentSomSkalBliVedlegg = PersistentDokumentId(input.id),
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @DeleteMapping("/{dokumentPersistentId}/vedlegg/{dokumentPersistentIdVedlegg}")
    fun fristillVedlegg(
        @PathVariable("dokumentId") persistentDokumentId: UUID,
        @PathVariable("dokumentIdVedlegg") persistentDokumentIdVedlegg: UUID,
    ): HovedDokumentView {
        logger.debug("Kall mottatt på fristillVedlegg for $persistentDokumentId og $persistentDokumentIdVedlegg")
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.frikobleVedlegg(
                persistentDokumentId = PersistentDokumentId(persistentDokumentId),
                persistentDokumentIdVedlegg = PersistentDokumentId(persistentDokumentIdVedlegg),
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @GetMapping
    fun findHovedDokumenter(
        @RequestParam("eksternReferanse") eksternReferanse: UUID,
    ): List<HovedDokumentView> {
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()
        return dokumentService.findHovedDokumenter(behandlingId = eksternReferanse, ident = ident)
            .map { dokumentMapper.mapToHovedDokumentView(it) }
    }

    @GetMapping("/smart")
    fun findSmartDokumenter(
        @RequestParam("eksternReferanse") eksternReferanse: UUID,
    ): List<DokumentView> {
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()
        return dokumentService.findSmartDokumenter(behandlingId = eksternReferanse, ident = ident)
            .map { dokumentMapper.mapToDokumentView(it) }
    }

    @PostMapping("/{hoveddokumentid}/ferdigstill")
    fun idempotentOpprettOgFerdigstillDokumentEnhetFraHovedDokument(hovedDokumentId: UUID): HovedDokumentView {
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.finnOgMarkerFerdigHovedDokument(
                hovedDokumentPersistentDokumentId = PersistentDokumentId(hovedDokumentId),
                ident = ident
            )
        )
    }
}