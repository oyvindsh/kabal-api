package no.nav.klage.dokument.api.controller


import io.swagger.annotations.Api
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
                dokumentType = DokumentType.of(body.dokumentType),
                behandlingId = body.eksternReferanse,
                opplastetFil = dokumenInputMapper.mapToMellomlagretDokument(
                    input.file,
                    DokumentType.of(body.dokumentType)
                ),
            )
        )
    }

    @PostMapping("/hoveddokumenter/smart")
    fun createSmartHoveddokument(
        @RequestBody body: HovedDokumentInput,
    ): HovedDokumentView {
        logger.debug("Kall mottatt på createSmartHoveddokument")
        return dokumentMapper.mapToHovedDokumentView(
            dokumentService.opprettOgMellomlagreNyttHoveddokument(
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
                dokumentType = DokumentType.of(body.dokumentType),
                behandlingId = body.eksternReferanse,
                opplastetFil = null,
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

    /*
    @PostMapping("/{dokumentEnhetId}/vedlegg")
    fun kobleVedlegg(
        @PathVariable("dokumentEnhetId") dokumentEnhetId: UUID,
        @RequestBody input: DokumentEnhetIdInput
    ): DokumentEnhetView {
        logger.debug("Kall mottatt på kobleVedlegg for $dokumentEnhetId")
        return dokumentEnhetMapper.mapToDokumentEnhetView(
            dokumentEnhetService.kobleVedlegg(
                dokumentEnhetId = dokumentEnhetId,
                dokumentEnhetSomSkalBliVedleggId = input.id,
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @DeleteMapping("/{dokumentEnhetId}/vedlegg/{vedleggId}")
    fun fristillVedlegg(
        @PathVariable("dokumentEnhetId") dokumentEnhetId: UUID,
        @PathVariable("vedleggId") vedleggId: UUID,
    ): DokumentEnhetView {
        logger.debug("Kall mottatt på fristillVedlegg for $dokumentEnhetId og $vedleggId")
        return dokumentEnhetMapper.mapToDokumentEnhetView(
            dokumentEnhetService.fristillVedlegg(
                dokumentEnhetId = dokumentEnhetId,
                vedleggId = vedleggId,
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

    @ResponseBody
    @GetMapping("/{dokumentEnhetId}/vedlegg/{vedleggId}")
    fun getVedlegg(
        @PathVariable("dokumentEnhetId") dokumentEnhetId: UUID,
        @PathVariable("vedleggId") vedleggId: UUID,
    ): ResponseEntity<ByteArray> {
        logger.debug("Kall mottatt på getVedlegg for $dokumentEnhetId og $vedleggId")
        return dokumentEnhetMapper.mapToByteArray(
            dokumentEnhetService.hentMellomlagretVedlegg(
                dokumentEnhetId = dokumentEnhetId,
                vedleggId = vedleggId,
                innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent()
            )
        )
    }

     */

    @GetMapping
    fun findHovedDokumenter(
        @RequestParam("eksternReferanse") eksternReferanse: UUID,
    ): List<HovedDokumentView> {
        return dokumentService.findHovedDokumenter(eksternReferanse)
            .map { dokumentMapper.mapToHovedDokumentView(it) }
    }

    @PostMapping("/{hoveddokumentid}/ferdigstill")
    fun idempotentOpprettOgFerdigstillDokumentEnhetFraHovedDokument(hovedDokumentId: UUID) {
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()
        val hovedDokument = dokumentService.finnOgMarkerFerdigHovedDokument(PersistentDokumentId(hovedDokumentId))
    }
}