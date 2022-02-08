package no.nav.klage.dokument.service

import no.nav.klage.dokument.clients.kabalsmarteditorapi.DefaultKabalSmartEditorApiGateway
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.*
import no.nav.klage.dokument.exceptions.DokumentValidationException
import no.nav.klage.dokument.repositories.HovedDokumentRepository
import no.nav.klage.dokument.repositories.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId
import no.nav.klage.dokument.repositories.getDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId
import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.clients.saf.graphql.Journalpost
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.klage.BehandlingAggregatFunctions.addSaksdokument
import no.nav.klage.oppgave.domain.klage.Endringslogginnslag
import no.nav.klage.oppgave.domain.klage.Felt
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class DokumentUnderArbeidService(
    private val hovedDokumentRepository: HovedDokumentRepository,
    private val attachmentValidator: MellomlagretDokumentValidatorService,
    private val mellomlagerService: MellomlagerService,
    private val smartEditorApiGateway: DefaultKabalSmartEditorApiGateway,
    private val behandlingService: BehandlingService,
    private val dokumentEnhetService: KabalDocumentGateway,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val safClient: SafGraphQlClient,

    ) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun finnOgMarkerFerdigHovedDokument(
        hovedDokumentPersistentDokumentId: PersistentDokumentId,
        ident: String
    ): HovedDokument {
        val hovedDokument = hovedDokumentRepository.findByPersistentDokumentId(hovedDokumentPersistentDokumentId)
            ?: throw DokumentValidationException("Dokument ikke funnet")

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)

        if (hovedDokument.erFerdigstilt()) {
            throw DokumentValidationException("Kan ikke endre dokumenttype på et dokument som er ferdigstilt")
        }

        if (hovedDokument.dokumentType == DokumentType.VEDLEGG) {
            throw DokumentValidationException("Kan ikke markere et vedlegg som ferdig")
        }

        return hovedDokument.apply { markerFerdigHvisIkkeAlleredeMarkertFerdig() }
    }

    fun opprettOgMellomlagreNyttHoveddokument(
        innloggetIdent: String,
        dokumentType: DokumentType,
        behandlingId: UUID,
        opplastetFil: MellomlagretDokument?,
        json: String?,
    ): HovedDokument {
        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(behandlingId)

        if (opplastetFil != null) {
            attachmentValidator.validateAttachment(opplastetFil)
            val mellomlagerId = mellomlagerService.uploadDocument(opplastetFil)
            return hovedDokumentRepository.save(
                HovedDokument(
                    mellomlagerId = mellomlagerId,
                    opplastet = LocalDateTime.now(),
                    size = opplastetFil.content.size.toLong(),
                    name = opplastetFil.title,
                    dokumentType = dokumentType,
                    behandlingId = behandlingId,
                )
            ).also {
                behandling.publishEndringsloggEvent(
                    saksbehandlerident = innloggetIdent,
                    felt = Felt.DOKUMENT_UNDER_ARBEID_OPPLASTET,
                    fraVerdi = null,
                    tilVerdi = it.opplastet.toString(),
                    tidspunkt = it.opplastet,
                    persistentDokumentId = it.persistentDokumentId,
                )
            }
        } else {
            if (json == null) {
                throw DokumentValidationException("Ingen json angitt")
            }
            val (smartEditorDokument, opplastet) =
                smartEditorApiGateway.createDocument(json, dokumentType, innloggetIdent)
            val mellomlagerId = mellomlagerService.uploadDocument(smartEditorDokument)
            return hovedDokumentRepository.save(
                HovedDokument(
                    mellomlagerId = mellomlagerId,
                    opplastet = opplastet,
                    size = smartEditorDokument.content.size.toLong(),
                    name = smartEditorDokument.title,
                    dokumentType = dokumentType,
                    behandlingId = behandlingId,
                    smartEditorId = smartEditorDokument.smartEditorId,
                )
            ).also {
                behandling.publishEndringsloggEvent(
                    saksbehandlerident = innloggetIdent,
                    felt = Felt.DOKUMENT_UNDER_ARBEID_OPPLASTET,
                    fraVerdi = null,
                    tilVerdi = it.opplastet.toString(),
                    tidspunkt = it.opplastet,
                    persistentDokumentId = it.persistentDokumentId,
                )
            }
        }
    }

    fun updateDokumentType(
        innloggetIdent: String,
        persistentDokumentId: PersistentDokumentId,
        dokumentType: DokumentType
    ): HovedDokument {

        //Skal ikke kunne endre dokumentType på vedlegg, så jeg spør her bare etter hoveddokumenter
        val hovedDokument = hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentId)
            ?: throw DokumentValidationException("Dokument ikke funnet")

        //Sjekker tilgang på behandlingsnivå:
        val behandling = behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)

        if (hovedDokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke endre dokumenttype på et dokument som er ferdigstilt")
        }
        hovedDokument.dokumentType = dokumentType
        return hovedDokument.also {
            behandling.publishEndringsloggEvent(
                saksbehandlerident = innloggetIdent,
                felt = Felt.DOKUMENT_UNDER_ARBEID_TYPE,
                fraVerdi = null,
                tilVerdi = it.opplastet.toString(),
                tidspunkt = it.opplastet,
                persistentDokumentId = it.persistentDokumentId,
            )
        }
    }

    fun hentMellomlagretDokument(
        persistentDokumentId: PersistentDokumentId,
        innloggetIdent: String
    ): MellomlagretDokument {
        val dokument: DokumentUnderArbeid =
            hovedDokumentRepository.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(
                persistentDokumentId
            ) ?: throw DokumentValidationException("Dokument ikke funnet")

        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandling(dokument.behandlingId)

        if (dokument.isStaleSmartEditorDokument()) {
            mellomlagreSmartEditorDokument(dokument.smartEditorId!!)
        }
        return mellomlagerService.getUploadedDocument(dokument.mellomlagerId)
    }

    fun hentMellomlagretDokumentSomSystembruker(persistentDokumentId: PersistentDokumentId): MellomlagretDokument {
        val dokument: DokumentUnderArbeid =
            hovedDokumentRepository.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(
                persistentDokumentId
            ) ?: throw DokumentValidationException("Dokument ikke funnet")
        return mellomlagerService.getUploadedDocumentAsSystemUser(dokument.mellomlagerId)
    }

    fun slettDokument(persistentDokumentId: PersistentDokumentId, innloggetIdent: String) {
        val hovedDokument: HovedDokument? = hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentId)
        if (hovedDokument != null) {

            //Sjekker tilgang på behandlingsnivå:
            val behandling = behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)

            if (hovedDokument.erMarkertFerdig()) {
                throw DokumentValidationException("Kan ikke slette et dokument som er ferdigstilt")
            }
            if (hovedDokument.harVedlegg()) {
                throw DokumentValidationException("Kan ikke slette DokumentEnhet med vedlegg")
            }
            if (hovedDokument.smartEditorId != null) {
                smartEditorApiGateway.deleteDocument(hovedDokument.smartEditorId!!)
            }
            hovedDokumentRepository.delete(hovedDokument)
            behandling.publishEndringsloggEvent(
                saksbehandlerident = innloggetIdent,
                felt = Felt.DOKUMENT_UNDER_ARBEID_OPPLASTET,
                fraVerdi = hovedDokument.opplastet.toString(),
                tilVerdi = null,
                tidspunkt = LocalDateTime.now(),
                persistentDokumentId = hovedDokument.persistentDokumentId,
            )

        } else {
            val hovedDokumentMedVedlegg =
                hovedDokumentRepository.findByVedleggPersistentDokumentId(persistentDokumentId)
                    ?: throw DokumentValidationException("Dokument ikke funnet")

            //Sjekker tilgang på behandlingsnivå:
            val behandling = behandlingService.getBehandlingForUpdate(hovedDokumentMedVedlegg.behandlingId)

            if (hovedDokumentMedVedlegg.erMarkertFerdig()) {
                throw DokumentValidationException("Kan ikke slette et dokument som er ferdigstilt")
            }
            if (hovedDokumentMedVedlegg.harVedlegg()) {
                throw DokumentValidationException("Kan ikke slette DokumentEnhet med vedlegg")

            }
            val vedlegg = hovedDokumentMedVedlegg.findDokumentUnderArbeidByPersistentDokumentId(persistentDokumentId)
                ?: throw DokumentValidationException("Dokument ikke funnet")
            if (vedlegg.smartEditorId != null) {
                smartEditorApiGateway.deleteDocument(vedlegg.smartEditorId!!)
            }
            hovedDokumentMedVedlegg.vedlegg.remove(vedlegg)
            behandling.publishEndringsloggEvent(
                saksbehandlerident = innloggetIdent,
                felt = Felt.DOKUMENT_UNDER_ARBEID_OPPLASTET,
                fraVerdi = vedlegg.opplastet.toString(),
                tilVerdi = null,
                tidspunkt = LocalDateTime.now(),
                persistentDokumentId = vedlegg.persistentDokumentId,
            )
        }

    }

    fun kobleVedlegg(
        persistentDokumentId: PersistentDokumentId,
        persistentDokumentIdHovedDokumentSomSkalBliVedlegg: PersistentDokumentId,
        innloggetIdent: String
    ): HovedDokument {
        val hovedDokument = hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentId)
            ?: throw DokumentValidationException("Dokument ikke funnet")

        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)
        //TODO: Skal det lages endringslogg på dette??

        if (hovedDokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke koble et dokument som er ferdigstilt")
        }

        val hovedDokumentSomSkalBliVedlegg =
            hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentIdHovedDokumentSomSkalBliVedlegg)
                ?: throw DokumentValidationException("Dokument ikke funnet")
        if (hovedDokumentSomSkalBliVedlegg.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke koble et dokument som er ferdigstilt")
        }
        if (hovedDokumentSomSkalBliVedlegg.dokumentType != DokumentType.VEDLEGG) {
            throw DokumentValidationException("Kan ikke koble et dokument ikke er et vedlegg")
        }
        if (hovedDokumentSomSkalBliVedlegg.harVedlegg()) {
            throw DokumentValidationException("Et dokument som selv har vedlegg kan ikke bli et vedlegg")
        }

        hovedDokumentRepository.delete(hovedDokumentSomSkalBliVedlegg)
        hovedDokument.vedlegg.add(hovedDokumentSomSkalBliVedlegg.toVedlegg())
        return hovedDokument
    }

    fun frikobleVedlegg(
        persistentDokumentId: PersistentDokumentId,
        persistentDokumentIdVedlegg: PersistentDokumentId,
        innloggetIdent: String
    ): HovedDokument {
        val hovedDokument = hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentId)
            ?: throw DokumentValidationException("Dokument ikke funnet")

        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandlingForUpdate(hovedDokument.behandlingId)
        //TODO: Skal det lages endringslogg på dette??

        if (hovedDokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke frikoble et dokument som er ferdigstilt")
        }

        val vedlegg =
            hovedDokument.findVedleggByPersistentDokumentId(persistentDokumentIdVedlegg)
                ?: throw DokumentValidationException("Dokument ikke funnet")

        hovedDokument.vedlegg.remove(vedlegg)
        return hovedDokumentRepository.save(vedlegg.toHovedDokument())
    }

    fun findHovedDokumenter(behandlingId: UUID, ident: String): SortedSet<HovedDokument> {
        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandling(behandlingId)

        return hovedDokumentRepository.findByBehandlingIdOrderByCreated(behandlingId)
    }

    fun findSmartDokumenter(behandlingId: UUID, ident: String): List<DokumentUnderArbeid> {
        //Sjekker tilgang på behandlingsnivå:
        behandlingService.getBehandling(behandlingId)

        return hovedDokumentRepository.findByBehandlingIdOrderByCreated(behandlingId).flatMap { it.vedlegg + it }
            .filter { it.smartEditorId != null }
    }

    //Denne kan kjøres asynkront vha en scheduled task. Er ikke ferdig koda
    fun opprettDokumentEnhet(hovedDokumentId: DokumentId) {

        val hovedDokument = hovedDokumentRepository.getById(hovedDokumentId)
        if (hovedDokument.dokumentEnhetId == null) {
            //TODO: Løp gjennom og refresh alle smarteditor-dokumenter
            val behandling = behandlingService.getBehandlingForUpdateBySystembruker(hovedDokument.behandlingId)
            val dokumentEnhetId = dokumentEnhetService.createKomplettDokumentEnhet(behandling, hovedDokument)
            hovedDokument.dokumentEnhetId = dokumentEnhetId
        }
    }

    fun ferdigstillDokumentEnhet(hovedDokumentId: DokumentId) {
        val hovedDokument = hovedDokumentRepository.getById(hovedDokumentId)
        val behandling: Behandling = behandlingService.getBehandlingForUpdateBySystembruker(hovedDokument.behandlingId)
        val journalpostId =
            dokumentEnhetService.fullfoerDokumentEnhet(dokumentEnhetId = hovedDokument.dokumentEnhetId!!)

        val journalpost = safClient.getJournalpostAsSystembruker(journalpostId.value)
        val saksdokumenter = journalpost.mapToSaksdokumenter()
        saksdokumenter.forEach { saksdokument ->
            val saksbehandlerIdent =
                behandling.tildelingHistorikk.maxByOrNull { it.tildeling.tidspunkt }?.tildeling?.saksbehandlerident
                    ?: "SYSTEMBRUKER" //TODO: Er dette innafor? Burde vi evt lagre ident i HovedDokument, så vi kan hente det derfra?
            behandling.addSaksdokument(saksdokument, saksbehandlerIdent)
                ?.also { applicationEventPublisher.publishEvent(it) }
        }
        hovedDokument.ferdigstillHvisIkkeAlleredeFerdigstilt()
    }

    fun getSmartEditorId(persistentDokumentId: PersistentDokumentId, readOnly: Boolean): UUID {
        val dokumentUnderArbeid =
            hovedDokumentRepository.getDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId(
                persistentDokumentId
            )

        //Sjekker tilgang på behandlingsnivå:
        if (readOnly) {
            behandlingService.getBehandling(dokumentUnderArbeid.behandlingId)
        } else {
            behandlingService.getBehandlingForUpdate(dokumentUnderArbeid.behandlingId)
        }

        return dokumentUnderArbeid.smartEditorId
            ?: throw DokumentValidationException("${persistentDokumentId.persistentDokumentId} er ikke et smarteditor dokument")
    }

    private fun mellomlagreSmartEditorDokument(smartEditorId: UUID) {
        mellomlagerService.uploadDocument(smartEditorApiGateway.getDocumentAsPDF(smartEditorId))
    }

    private fun DokumentUnderArbeid.isStaleSmartEditorDokument() =
        this.smartEditorId != null && !this.erMarkertFerdig() && smartEditorApiGateway.isMellomlagretDokumentStale(
            this.smartEditorId!!,
            this.opplastet
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
        persistentDokumentId: PersistentDokumentId,
    ) {
        listOfNotNull(
            this.endringslogg(
                saksbehandlerident = saksbehandlerident,
                felt = Felt.DOKUMENT_UNDER_ARBEID_ID,
                fraVerdi = fraVerdi.let { persistentDokumentId.persistentDokumentId.toString() },
                tilVerdi = tilVerdi.let { persistentDokumentId.persistentDokumentId.toString() },
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


