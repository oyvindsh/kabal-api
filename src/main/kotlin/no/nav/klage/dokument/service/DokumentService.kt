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
    private val attachmentValidator: AttachmentValidatorService,
    private val mellomlagerService: MellomlagerService,
    private val smartEditorClient: SmartEditorClient,
    private val behandlingService: BehandlingService,
    private val dokumentEnhetService: KabalDocumentGateway,
) {

    fun finnOgMarkerFerdigHovedDokument(hovedDokumentPersistentDokumentId: PersistentDokumentId): HovedDokument {
        return hovedDokumentRepository.findByPersistentDokumentId(hovedDokumentPersistentDokumentId)
            .apply { markerFerdigHvisIkkeAlleredeMarkertFerdig() }
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
                    smartEditorId = smartEditorDokument.id,
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
        TODO("Not yet implemented")
        //Sjekke om det er markertSomFerdigstilt
        //Hvis hoveddokument, sjekk om det har vedlegg
        
    }

    fun findHovedDokumenter(behandlingId: UUID): List<HovedDokument> {
        return hovedDokumentRepository.findByBehandlingId(behandlingId)
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