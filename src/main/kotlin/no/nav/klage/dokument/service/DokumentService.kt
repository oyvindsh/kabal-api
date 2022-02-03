package no.nav.klage.dokument.service

import no.nav.klage.dokument.clients.smarteditorapi.SmartEditorClient
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.MellomlagretMultipartFile
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentType
import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.dokument.domain.dokumenterunderarbeid.HovedDokument
import no.nav.klage.dokument.domain.dokumenterunderarbeid.PersistentDokumentId
import no.nav.klage.dokument.exceptions.DokumentValidationException
import no.nav.klage.dokument.repositories.HovedDokumentRepository
import no.nav.klage.dokument.repositories.findDokumentUnderArbeidByPersistentDokumentIdOrVedleggPersistentDokumentId
import no.nav.klage.oppgave.clients.kabaldocument.KabalDocumentGateway
import no.nav.klage.oppgave.service.BehandlingService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import javax.transaction.Transactional

@Service
@Transactional
class DokumentService(
    private val hovedDokumentRepository: HovedDokumentRepository,
    private val attachmentValidator: MellomlagretDokumentValidatorService,
    private val mellomlagerService: MellomlagerService,
    private val smartEditorClient: SmartEditorClient,
    private val behandlingService: BehandlingService,
    private val dokumentEnhetService: KabalDocumentGateway,
) {

    fun finnOgMarkerFerdigHovedDokument(
        hovedDokumentPersistentDokumentId: PersistentDokumentId,
        ident: String
    ): HovedDokument {
        return hovedDokumentRepository.findByPersistentDokumentId(hovedDokumentPersistentDokumentId)
            ?.apply { markerFerdigHvisIkkeAlleredeMarkertFerdig() }
            ?: throw DokumentValidationException("Dokument ikke funnet")
    }

    fun opprettOgMellomlagreNyttHoveddokument(
        innloggetIdent: String,
        dokumentType: DokumentType,
        behandlingId: UUID,
        opplastetFil: MellomlagretDokument?
    ): HovedDokument {

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
                    vedlegg = mutableListOf(),
                )
            )
        } else {
            val smartEditorDokument = smartEditorClient.createDocument(dokumentType, innloggetIdent)
            val mellomlagerId = mellomlagerService.uploadDocument(smartEditorDokument)
            return hovedDokumentRepository.save(
                HovedDokument(
                    mellomlagerId = mellomlagerId,
                    opplastet = LocalDateTime.now(),
                    size = smartEditorDokument.content.size.toLong(),
                    name = smartEditorDokument.title,
                    dokumentType = dokumentType,
                    behandlingId = behandlingId,
                    smartEditorId = smartEditorDokument.smartEditorId,
                    vedlegg = mutableListOf(),
                )
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

            if (hovedDokument.erMarkertFerdig()) {
                throw DokumentValidationException("Kan ikke slette et dokument som er ferdigstilt")
            }
            if (hovedDokument.harVedlegg()) {
                throw DokumentValidationException("Kan ikke slette DokumentEnhet med vedlegg")
            }
            hovedDokumentRepository.delete(hovedDokument)
        } else {
            val hovedDokumentMedVedlegg =
                hovedDokumentRepository.findByVedleggPersistentDokumentId(persistentDokumentId)
                    ?: throw DokumentValidationException("Dokument ikke funnet")
            if (hovedDokumentMedVedlegg.erMarkertFerdig()) {
                throw DokumentValidationException("Kan ikke slette et dokument som er ferdigstilt")
            }
            if (hovedDokumentMedVedlegg.harVedlegg()) {
                throw DokumentValidationException("Kan ikke slette DokumentEnhet med vedlegg")

            }
            val vedlegg = hovedDokumentMedVedlegg.findDokumentUnderArbeidByPersistentDokumentId(persistentDokumentId)
                ?: throw DokumentValidationException("Dokument ikke funnet")
            hovedDokumentMedVedlegg.vedlegg.remove(vedlegg)
        }

    }

    fun findHovedDokumenter(behandlingId: UUID, ident: String): List<HovedDokument> {
        return hovedDokumentRepository.findByBehandlingId(behandlingId)
    }

    fun kobleVedlegg(
        persistentDokumentId: PersistentDokumentId,
        persistentDokumentIdHovedDokumentSomSkalBliVedlegg: PersistentDokumentId,
        innloggetIdent: String
    ): HovedDokument {
        val hovedDokument = hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentId)
            ?: throw DokumentValidationException("Dokument ikke funnet")
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
        if (hovedDokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke frikoble et dokument som er ferdigstilt")
        }

        val vedlegg =
            hovedDokument.findVedleggByPersistentDokumentId(persistentDokumentIdVedlegg)
                ?: throw DokumentValidationException("Dokument ikke funnet")

        hovedDokument.vedlegg.remove(vedlegg)
        return hovedDokumentRepository.save(vedlegg.toHovedDokument()) //TODO: Skal vi endre på dokumenttypen?
    }

    fun findSmartDokumenter(behandlingId: UUID, ident: String): List<DokumentUnderArbeid> {
        return hovedDokumentRepository.findByBehandlingId(behandlingId).flatMap { it.vedlegg + it }
            .filter { it.smartEditorId != null }
    }

    fun updateDokumentType(persistentDokumentId: PersistentDokumentId, dokumentType: DokumentType): HovedDokument {
        //Skal ikke kunne endre dokumentType på vedlegg, så jeg spør her bare etter hoveddokumenter
        val hovedDokument = hovedDokumentRepository.findByPersistentDokumentId(persistentDokumentId)
            ?: throw DokumentValidationException("Dokument ikke funnet")
        if (hovedDokument.erMarkertFerdig()) {
            throw DokumentValidationException("Kan ikke endre dokumenttype på et dokument som er ferdigstilt")
        }
        hovedDokument.dokumentType = dokumentType
        return hovedDokument
    }

    //Denne kan kjøres asynkront vha en scheduled task. Er ikke ferdig koda
    fun ferdigstillMarkerteHovedDokumenter() {

        val liste = hovedDokumentRepository.findByMarkertFerdigNotNullAndFerdigstiltNull()
        liste.forEach {
            val hovedDokument = it

            val behandling = behandlingService.getBehandling(hovedDokument.behandlingId)
            //TODO: Ønsker meg egentlig en måte å gjøre alt det følgende i en swung..
            val dokumentEnhetId = dokumentEnhetService.createDokumentEnhet(behandling)
            val mellomlagretDokument =
                hentMellomlagretDokumentSomSystembruker(hovedDokument.persistentDokumentId) //Må gjøres som systembruker
            val multipartFile =
                MellomlagretMultipartFile(mellomlagretDokument)
            dokumentEnhetService.uploadHovedDokument(dokumentEnhetId, multipartFile)
            //TODO: Må også uploade vedlegg


            val ferdigstiltDokumentEnhet = dokumentEnhetService.fullfoerDokumentEnhet(dokumentEnhetId = dokumentEnhetId)
            //Feiler det får vi en 500..
        }
    }
}