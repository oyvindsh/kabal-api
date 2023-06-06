package no.nav.klage.dokument.service

import jakarta.transaction.Transactional
import no.nav.klage.dokument.api.view.DocumentValidationResponse
import no.nav.klage.dokument.clients.kabaljsontopdf.KabalJsonToPdfClient
import no.nav.klage.dokument.clients.kabalsmarteditorapi.DefaultKabalSmartEditorApiGateway
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.OpplastetMellomlagretDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeidJournalpostId
import no.nav.klage.dokument.exceptions.DokumentValidationException
import no.nav.klage.dokument.exceptions.JsonToPdfValidationException
import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.Brevmottakertype
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.clients.saf.graphql.Journalpost
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.events.DokumentFerdigstiltAvSaksbehandler
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.addSaksdokument
import no.nav.klage.oppgave.domain.klage.Endringslogginnslag
import no.nav.klage.oppgave.domain.klage.Felt
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.InnloggetSaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class DokumentUnderArbeidService(
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val attachmentValidator: MellomlagretDokumentValidatorService,
    private val mellomlagerService: MellomlagerService,
    private val smartEditorApiGateway: DefaultKabalSmartEditorApiGateway,
    private val kabalJsonToPdfClient: KabalJsonToPdfClient,
    private val behandlingService: BehandlingService,
    private val dokumentEnhetService: KabalDocumentGateway,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val safClient: SafGraphQlClient,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val SYSTEMBRUKER = "SYSTEMBRUKER"
    }

    fun opprettOgMellomlagreNyttHoveddokument(
        behandlingId: UUID,
        dokumentType: DokumentType,
        opplastetFil: MellomlagretDokument?,
        innloggetIdent: String,
        tittel: String,
    ): DokumentUnderArbeid {
        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(behandlingId)

        if (opplastetFil == null) {
            throw DokumentValidationException("No file uploaded")
        }

        attachmentValidator.validateAttachment(opplastetFil)
        val mellomlagerId = mellomlagerService.uploadDocument(opplastetFil)
        val hovedDokument = dokumentUnderArbeidRepository.save(
            DokumentUnderArbeid(
                mellomlagerId = mellomlagerId,
                opplastet = LocalDateTime.now(),
                size = opplastetFil.content.size.toLong(),
                name = tittel,
                dokumentType = dokumentType,
                behandlingId = behandlingId,
                smartEditorId = null,
                smartEditorTemplateId = null,
                smartEditorVersion = null,
            )
        )
        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.DOKUMENT_UNDER_ARBEID_OPPLASTET,
            fraVerdi = null,
            tilVerdi = hovedDokument.opplastet.toString(),
            tidspunkt = hovedDokument.opplastet!!,
            dokumentId = hovedDokument.id,
        )
        return hovedDokument
    }

    fun opprettSmartdokument(
        behandlingId: UUID,
        dokumentType: DokumentType,
        json: String?,
        smartEditorTemplateId: String?,
        smartEditorVersion: Int?,
        innloggetIdent: String,
        tittel: String,
    ): DokumentUnderArbeid {
        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(behandlingId)

        if (json == null) {
            throw DokumentValidationException("Ingen json angitt")
        }
        val smartEditorDocumentId =
            smartEditorApiGateway.createDocument(
                json = json,
                dokumentType = dokumentType,
                innloggetIdent = innloggetIdent,
                documentTitle = tittel
            )

        val hovedDokument = dokumentUnderArbeidRepository.save(
            DokumentUnderArbeid(
                mellomlagerId = null,
                opplastet = null,
                size = null,
                name = tittel,
                dokumentType = dokumentType,
                behandlingId = behandlingId,
                smartEditorId = smartEditorDocumentId,
                smartEditorTemplateId = smartEditorTemplateId,
                smartEditorVersion = smartEditorVersion,
            )
        )
        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.SMARTDOKUMENT_OPPRETTET,
            fraVerdi = null,
            tilVerdi = hovedDokument.created.toString(),
            tidspunkt = hovedDokument.created,
            dokumentId = hovedDokument.id,
        )
        return hovedDokument
    }

    fun getDokumentUnderArbeid(dokumentId: UUID) = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

    fun updateDokumentType(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        dokumentType: DokumentType,
        innloggetIdent: String
    ): DokumentUnderArbeid {

        val dokumentUnderArbeid = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(dokumentUnderArbeid.behandlingId)

        if (dokumentUnderArbeid.parentId != null) {
            //Vi skal ikke kunne endre dokumentType på vedlegg
            throw DokumentValidationException("Man kan ikke endre dokumentType på vedlegg")
        }

        if (dokumentUnderArbeid.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke endre dokumenttype på et dokument som er ferdigstilt")
        }

        val previousValue = dokumentUnderArbeid.dokumentType
        dokumentUnderArbeid.dokumentType = dokumentType
        dokumentUnderArbeid.modified = LocalDateTime.now()
        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.DOKUMENT_UNDER_ARBEID_TYPE,
            fraVerdi = previousValue.id,
            tilVerdi = dokumentUnderArbeid.modified.toString(),
            tidspunkt = dokumentUnderArbeid.modified,
            dokumentId = dokumentUnderArbeid.id,
        )
        return dokumentUnderArbeid
    }

    fun updateDokumentTitle(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        dokumentTitle: String,
        innloggetIdent: String
    ): DokumentUnderArbeid {

        val dokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(dokument.behandlingId)

        if (dokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke endre tittel på et dokument som er ferdigstilt")
        }

        val oldValue = dokument.name
        dokument.name = dokumentTitle
        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.DOKUMENT_UNDER_ARBEID_NAME,
            fraVerdi = oldValue,
            tilVerdi = dokument.name,
            tidspunkt = LocalDateTime.now(),
            dokumentId = dokument.id,
        )
        return dokument
    }

    fun updateSmartEditorVersion(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        version: Int,
        innloggetIdent: String
    ): DokumentUnderArbeid {
        val dokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        val smartEditorVersion = dokument.smartEditorVersion
        if (smartEditorVersion == version) {
            return dokument
        } else if (smartEditorVersion != null) {
            if (smartEditorVersion > version) {
                throw DokumentValidationException("Input-versjon er eldre enn lagret versjon på smartdokument.")
            }
        }

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(dokument.behandlingId)

        if (dokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke endre smartEditorVersion på et dokument som er ferdigstilt")
        }

        val oldValue = dokument.smartEditorVersion
        dokument.smartEditorVersion = version
        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.SMARTDOKUMENT_VERSION,
            fraVerdi = oldValue?.toString(),
            tilVerdi = dokument.smartEditorVersion?.toString(),
            tidspunkt = LocalDateTime.now(),
            dokumentId = dokument.id,
        )
        return dokument
    }

    fun validateDocumentNotFinalized(
        dokumentId: UUID
    ) {
        val dokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)
        if (dokument.erMarkertFerdig()) {
            throw DokumentValidationException("Dokument er allerede ferdigstilt.")
        }
    }

    fun updateSmartEditorTemplateId(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        templateId: String,
        innloggetIdent: String
    ): DokumentUnderArbeid {
        val dokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        if (dokument.smartEditorTemplateId == templateId) {
            return dokument
        }

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(dokument.behandlingId)

        if (dokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke endre smartEditorTemplateId på et dokument som er ferdigstilt")
        }

        val oldValue = dokument.smartEditorTemplateId
        dokument.smartEditorTemplateId = templateId
        dokument.modified = LocalDateTime.now()
        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.SMARTDOKUMENT_TEMPLATE_ID,
            fraVerdi = oldValue,
            tilVerdi = dokument.smartEditorTemplateId,
            tidspunkt = LocalDateTime.now(),
            dokumentId = dokument.id,
        )
        return dokument
    }

    private fun updateJournalposter(
        behandlingId: UUID,
        dokumentId: UUID,
        journalpostIdSet: Set<DokumentUnderArbeidJournalpostId>
    ): DokumentUnderArbeid {
        val dokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        val behandling = behandlingService.getBehandlingForUpdateBySystembruker(behandlingId)

        val oldValue = dokument.journalposter
        dokument.journalposter.clear()
        dokument.journalposter.addAll(journalpostIdSet)

        behandling.publishEndringsloggEvent(
            saksbehandlerident = SYSTEMBRUKER,
            felt = Felt.DOKUMENT_UNDER_ARBEID_JOURNALPOST_ID,
            fraVerdi = oldValue.joinToString { it.journalpostId },
            tilVerdi = dokument.journalposter.joinToString { it.journalpostId },
            tidspunkt = LocalDateTime.now(),
            dokumentId = dokument.id,
        )

        return dokument
    }

    fun validateSmartDokument(
        dokumentId: UUID
    ): List<DocumentValidationResponse> {
        val documentValidationResults = mutableListOf<DocumentValidationResponse>()

        val hovedDokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)
        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokument.id)
        if (hovedDokument.smartEditorId != null) {
            documentValidationResults += validateSingleDocument(hovedDokument)
        }
        vedlegg.forEach {
            if (it.smartEditorId != null) {
                documentValidationResults += validateSingleDocument(it)
            }
        }

        return documentValidationResults
    }

    private fun validateSingleDocument(dokument: DokumentUnderArbeid): DocumentValidationResponse {
        logger.debug("Getting json document, dokumentId: ${dokument.id}")
        val documentJson = smartEditorApiGateway.getDocumentAsJson(dokument.smartEditorId!!)
        logger.debug("Validating json document in kabalJsontoPdf, dokumentId: ${dokument.id}")
        val response = kabalJsonToPdfClient.validateJsonDocument(documentJson)
        return DocumentValidationResponse(
            dokumentId = dokument.id.toString(),
            errors = response.errors.map {
                DocumentValidationResponse.DocumentValidationError(
                    type = it.type,
                    paths = it.paths,
                )
            }
        )
    }

    fun finnOgMarkerFerdigHovedDokument(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        ident: String,
        brevmottakertyper: Set<Brevmottakertype>
    ): DokumentUnderArbeid {
        val documentValidationErrors = validateSmartDokument(dokumentId)
        if (documentValidationErrors.any { it.errors.isNotEmpty() }) {
            throw JsonToPdfValidationException(
                msg = "Validation error from json to pdf",
                errors = documentValidationErrors
            )
        }

        val hovedDokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        if (hovedDokument.dokumentType != DokumentType.NOTAT && brevmottakertyper.isEmpty()) {
            throw DokumentValidationException("Brevmottakere må være satt")
        }

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)

        if (hovedDokument.erMarkertFerdig() || hovedDokument.erFerdigstilt()) {
            throw DokumentValidationException("Kan ikke endre dokumenttype på et dokument som er ferdigstilt")
        }

        if (hovedDokument.parentId != null) {
            throw DokumentValidationException("Kan ikke markere et vedlegg som ferdig")
        }

        val now = LocalDateTime.now()
        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokument.id)
        hovedDokument.markerFerdigHvisIkkeAlleredeMarkertFerdig(tidspunkt = now, saksbehandlerIdent = ident)
        hovedDokument.brevmottakertyper = brevmottakertyper.toMutableSet()
        vedlegg.forEach { it.markerFerdigHvisIkkeAlleredeMarkertFerdig(tidspunkt = now, saksbehandlerIdent = ident) }

        //Etter at et dokument er markert som ferdig skal det ikke kunne endres. Vi henter derfor en snapshot av tilstanden slik den er nå
        if (hovedDokument.smartEditorId != null) {
            mellomlagreNyVersjonAvSmartEditorDokument(hovedDokument)
        }
        vedlegg.forEach {
            if (it.smartEditorId != null) {
                mellomlagreNyVersjonAvSmartEditorDokument(it)
            }
        }

        behandling.publishEndringsloggEvent(
            saksbehandlerident = ident,
            felt = Felt.DOKUMENT_UNDER_ARBEID_MARKERT_FERDIG,
            fraVerdi = null,
            tilVerdi = hovedDokument.markertFerdig.toString(),
            tidspunkt = LocalDateTime.now(),
            dokumentId = hovedDokument.id,
        )

        behandling.publishEndringsloggEvent(
            saksbehandlerident = ident,
            felt = Felt.DOKUMENT_UNDER_ARBEID_BREVMOTTAKER_TYPER,
            fraVerdi = null,
            tilVerdi = hovedDokument.brevmottakertyper.joinToString { it.id },
            tidspunkt = LocalDateTime.now(),
            dokumentId = hovedDokument.id,
        )

        applicationEventPublisher.publishEvent(DokumentFerdigstiltAvSaksbehandler(hovedDokument))

        return hovedDokument
    }


    fun hentOgMellomlagreDokument(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        innloggetIdent: String
    ): MellomlagretDokument {
        val dokument =
            dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandling(dokument.behandlingId)

        if (dokument.isStaleSmartEditorDokument()) {
            mellomlagreNyVersjonAvSmartEditorDokument(dokument)
        }

        return OpplastetMellomlagretDokument(
            title = dokument.name,
            content = mellomlagerService.getUploadedDocument(dokument.mellomlagerId!!),
            contentType = MediaType.APPLICATION_PDF
        )
    }

    fun slettDokument(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        innloggetIdent: String,
    ) {
        val dokumentUnderArbeid = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandling(
            behandlingId = dokumentUnderArbeid.behandlingId,
        )

        if (dokumentUnderArbeid.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke slette et dokument som er ferdigstilt")
        }

        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(dokumentUnderArbeid.id)
        if (vedlegg.isNotEmpty()) {
            throw DokumentValidationException("Kan ikke slette dokument med vedlegg")
        }
        if (dokumentUnderArbeid.smartEditorId != null) {
            smartEditorApiGateway.deleteDocument(dokumentUnderArbeid.smartEditorId!!)
        }
        dokumentUnderArbeidRepository.delete(dokumentUnderArbeid)

        if (dokumentUnderArbeid.mellomlagerId != null) {
            mellomlagerService.deleteDocument(dokumentUnderArbeid.mellomlagerId!!)
        }

        behandling.publishEndringsloggEvent(
            saksbehandlerident = innloggetIdent,
            felt = Felt.DOKUMENT_UNDER_ARBEID_OPPLASTET,
            fraVerdi = dokumentUnderArbeid.opplastet.toString(),
            tilVerdi = null,
            tidspunkt = LocalDateTime.now(),
            dokumentId = dokumentUnderArbeid.id,
        )
    }

    fun kobleVedlegg(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        dokumentIdHovedDokumentSomSkalBliVedlegg: UUID,
        innloggetIdent: String
    ): DokumentUnderArbeid {
        val hovedDokument = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)
        //TODO: Skal det lages endringslogg på dette??

        if (hovedDokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke koble et dokument som er ferdigstilt")
        }

        val hovedDokumentSomSkalBliVedlegg =
            dokumentUnderArbeidRepository.getReferenceById(dokumentIdHovedDokumentSomSkalBliVedlegg)

        if (hovedDokumentSomSkalBliVedlegg.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke koble et dokument som er ferdigstilt")
        }

        val vedlegg =
            dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokumentSomSkalBliVedlegg.id)
        if (vedlegg.isNotEmpty()) {
            throw DokumentValidationException("Et dokument som selv har vedlegg kan ikke bli et vedlegg")
        }
        hovedDokumentSomSkalBliVedlegg.parentId = hovedDokument.id
        return hovedDokumentSomSkalBliVedlegg
    }

    fun frikobleVedlegg(
        behandlingId: UUID, //Kan brukes i finderne for å "være sikker", men er egentlig overflødig..
        dokumentId: UUID,
        innloggetIdent: String
    ): DokumentUnderArbeid {
        val vedlegg = dokumentUnderArbeidRepository.getReferenceById(dokumentId)

        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandlingForUpdate(vedlegg.behandlingId)
        //TODO: Skal det lages endringslogg på dette??

        if (vedlegg.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke frikoble et dokument som er ferdigstilt")
        }

        vedlegg.parentId = null
        return vedlegg
    }

    fun findDokumenterNotFinished(behandlingId: UUID): SortedSet<DokumentUnderArbeid> {
        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandling(behandlingId)

        return dokumentUnderArbeidRepository.findByBehandlingIdAndFerdigstiltIsNullOrderByCreated(behandlingId)
    }

    fun getSmartDokumenterUnderArbeid(behandlingId: UUID, ident: String): SortedSet<DokumentUnderArbeid> {
        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandling(behandlingId)

        return dokumentUnderArbeidRepository.findByBehandlingIdAndSmartEditorIdNotNullAndMarkertFerdigIsNullOrderByCreated(
            behandlingId
        )
    }

    fun opprettDokumentEnhet(hovedDokumentId: UUID): DokumentUnderArbeid {
        val hovedDokument = dokumentUnderArbeidRepository.getReferenceById(hovedDokumentId)
        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokument.id)
        //Denne er alltid sann
        if (hovedDokument.dokumentEnhetId == null) {
            //Vi vet at smartEditor-dokumentene har en oppdatert snapshot i mellomlageret fordi det ble fikset i finnOgMarkerFerdigHovedDokument
            val behandling = behandlingService.getBehandlingForUpdateBySystembruker(hovedDokument.behandlingId)
            val dokumentEnhetId = dokumentEnhetService.createKomplettDokumentEnhet(behandling, hovedDokument, vedlegg)
            hovedDokument.dokumentEnhetId = dokumentEnhetId
        }
        return hovedDokument
    }

    fun ferdigstillDokumentEnhet(hovedDokumentId: UUID): DokumentUnderArbeid {
        val hovedDokument = dokumentUnderArbeidRepository.getReferenceById(hovedDokumentId)
        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(hovedDokument.id)
        val behandling: Behandling = behandlingService.getBehandlingForUpdateBySystembruker(hovedDokument.behandlingId)
        val documentInfoList =
            dokumentEnhetService.fullfoerDokumentEnhet(dokumentEnhetId = hovedDokument.dokumentEnhetId!!)

        documentInfoList.forEach { documentInfo ->
            val journalpost = safClient.getJournalpostAsSystembruker(documentInfo.journalpostId.value)

            val saksdokumenter = journalpost.mapToSaksdokumenter()
            saksdokumenter.forEach { saksdokument ->
                val saksbehandlerIdent = SYSTEMBRUKER
                behandling.addSaksdokument(saksdokument, saksbehandlerIdent)
                    ?.also { applicationEventPublisher.publishEvent(it) }
            }
        }

        updateJournalposter(
            behandling.id,
            hovedDokumentId,
            HashSet(documentInfoList.map { DokumentUnderArbeidJournalpostId(journalpostId = it.journalpostId.value) })
        )

        val now = LocalDateTime.now()

        hovedDokument.ferdigstillHvisIkkeAlleredeFerdigstilt(now)
        if (hovedDokument.smartEditorId != null) {
            try {
                smartEditorApiGateway.deleteDocument(hovedDokument.smartEditorId!!)
            } catch(e: Exception) {
                logger.warn("Couldn't delete hoveddokument from smartEditorApi", e)
            }
        }

        vedlegg.forEach {
            it.ferdigstillHvisIkkeAlleredeFerdigstilt(now)
            try {
                smartEditorApiGateway.deleteDocument(it.smartEditorId!!)
            } catch(e: Exception) {
                logger.warn("Couldn't delete vedlegg from smartEditorApi", e)
            }
        }

        return hovedDokument
    }

    fun getSmartEditorId(dokumentId: UUID, readOnly: Boolean): UUID {
        val dokumentUnderArbeid = dokumentUnderArbeidRepository.getReferenceById(dokumentId)
        val ident = innloggetSaksbehandlerService.getInnloggetIdent()

        //Sjekker tilgang på behandlingsnivå:
        if (readOnly) {
            behandlingService.getBehandling(dokumentUnderArbeid.behandlingId)
        } else {
            behandlingService.getBehandlingForSmartEditor(
                behandlingId = dokumentUnderArbeid.behandlingId,
                utfoerendeSaksbehandlerIdent = ident,
            )
        }

        return dokumentUnderArbeid.smartEditorId
            ?: throw DokumentValidationException("$dokumentId er ikke et smarteditor dokument")
    }

    private fun mellomlagreNyVersjonAvSmartEditorDokument(dokument: DokumentUnderArbeid) {
        val documentJson = smartEditorApiGateway.getDocumentAsJson(dokument.smartEditorId!!)
        val pdfDocument = kabalJsonToPdfClient.getPDFDocument(documentJson)
        val mellomlagerId =
            mellomlagerService.uploadByteArray(
                tittel = dokument.name,
                content = pdfDocument.bytes
            )

        if (dokument.mellomlagerId != null) {
            mellomlagerService.deleteDocument(dokument.mellomlagerId!!)
        }

        dokument.mellomlagerId = mellomlagerId
        dokument.size = pdfDocument.bytes.size.toLong()
        dokument.opplastet = LocalDateTime.now()
    }

    private fun DokumentUnderArbeid.isStaleSmartEditorDokument() =
        this.smartEditorId != null && !this.erMarkertFerdig() && smartEditorApiGateway.isMellomlagretDokumentStale(
            smartEditorId = this.smartEditorId!!,
            sistOpplastet = this.opplastet
        )

    private fun Behandling.endringslogg(
        saksbehandlerident: String,
        felt: Felt,
        fraVerdi: String?,
        tilVerdi: String?,
        tidspunkt: LocalDateTime
    ): Endringslogginnslag? {
        return Endringslogginnslag.endringslogg(
            saksbehandlerident,
            felt,
            fraVerdi,
            tilVerdi,
            this.id,
            tidspunkt
        )
    }

    private fun Behandling.publishEndringsloggEvent(
        saksbehandlerident: String,
        felt: Felt,
        fraVerdi: String?,
        tilVerdi: String?,
        tidspunkt: LocalDateTime,
        dokumentId: UUID,
    ) {
        listOfNotNull(
            this.endringslogg(
                saksbehandlerident = saksbehandlerident,
                felt = Felt.DOKUMENT_UNDER_ARBEID_ID,
                fraVerdi = fraVerdi.let { dokumentId.toString() },
                tilVerdi = tilVerdi.let { dokumentId.toString() },
                tidspunkt = tidspunkt,
            ),
            this.endringslogg(
                saksbehandlerident = saksbehandlerident,
                felt = felt,
                fraVerdi = fraVerdi,
                tilVerdi = tilVerdi,
                tidspunkt = tidspunkt,
            )
        ).let {
            applicationEventPublisher.publishEvent(
                BehandlingEndretEvent(
                    behandling = this,
                    endringslogginnslag = it
                )
            )
        }
    }

    private fun Journalpost?.mapToSaksdokumenter(): List<Saksdokument> {
        return this?.dokumenter?.map {
            Saksdokument(
                journalpostId = this.journalpostId,
                dokumentInfoId = it.dokumentInfoId
            )
        } ?: emptyList()
    }
}


