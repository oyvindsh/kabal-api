package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.*
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.clients.saf.rest.SafRestClient
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafRestClient,
    private val klagebehandlingService: KlagebehandlingService,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val dokumentMapper = DokumentMapper()
    }

    fun fetchDokumentlisteForKlagebehandling(
        klagebehandlingId: UUID,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        val klagebehandling: Klagebehandling = klagebehandlingService.getKlagebehandling(klagebehandlingId)

        if (klagebehandling.foedselsnummer != null) {
            val valgteJournalposter =
                klagebehandling.saksdokumenter.map { it.journalpostId }.toHashSet()
            val dokumentoversiktBruker: DokumentoversiktBruker =
                safGraphQlClient.getDokumentoversiktBruker(klagebehandling.foedselsnummer, pageSize, previousPageRef)
            return DokumenterResponse(
                dokumenter = dokumentoversiktBruker.journalposter.map { journalpost ->
                    dokumentMapper.mapJournalpost(
                        journalpost,
                        valgteJournalposter.contains(journalpost.journalpostId)
                    )
                },
                pageReference = if (dokumentoversiktBruker.sideInfo.finnesNesteSide) {
                    dokumentoversiktBruker.sideInfo.sluttpeker
                } else {
                    null
                }
            )
        } else {
            return DokumenterResponse(dokumenter = emptyList(), pageReference = null)
        }
    }

    fun fetchJournalpostIderConnectedToKlagebehandling(klagebehandlingId: UUID): List<String> =
        klagebehandlingService.getKlagebehandling(klagebehandlingId).saksdokumenter.map { it.journalpostId }

    fun fetchJournalposterConnectedToKlagebehandling(klagebehandlingId: UUID): DokumenterResponse =
        klagebehandlingService.getKlagebehandling(klagebehandlingId).saksdokumenter
            .mapNotNull { safGraphQlClient.getJournalpost(it.journalpostId) }
            .map { dokumentMapper.mapJournalpost(it, true) }
            .let { DokumenterResponse(dokumenter = it, pageReference = null) }

    fun connectJournalpostToKlagebehandling(
        klagebehandlingId: UUID,
        journalpostId: String,
        saksbehandlerIdent: String
    ) {
        validateJournalpostExists(journalpostId)

        klagebehandlingService.addJournalpost(
            klagebehandlingId,
            journalpostId,
            saksbehandlerIdent
        )
    }

    fun disconnectJournalpostFromKlagebehandling(
        klagebehandlingId: UUID,
        journalpostId: String,
        saksbehandlerIdent: String
    ) = klagebehandlingService.removeJournalpost(klagebehandlingId, journalpostId, saksbehandlerIdent)


    private fun validateJournalpostExists(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpost(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun getArkivertDokument(journalpostId: String, dokumentInfoId: String): ArkivertDokument {
        return safRestClient.getDokument(dokumentInfoId, journalpostId)
    }

}

class DokumentMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Har ikke tatt høyde for skjerming, ref https://confluence.adeo.no/pages/viewpage.action?pageId=320364687
    fun mapJournalpost(journalpost: Journalpost, isConnected: Boolean): DokumentReferanse {

        val hoveddokument = journalpost.dokumenter?.firstOrNull()

        val dokumentReferanse = DokumentReferanse(
            tittel = hoveddokument?.tittel,
            tema = journalpost.temanavn,
            registrert = journalpost.datoOpprettet.toLocalDate(),
            dokumentInfoId = hoveddokument?.dokumentInfoId,
            journalpostId = journalpost.journalpostId,
            harTilgangTilArkivvariant = harTilgangTilArkivvariant(hoveddokument),
            valgt = isConnected
        )

        dokumentReferanse.vedlegg.addAll(getVedlegg(journalpost))

        return dokumentReferanse
    }

    private fun getVedlegg(journalpost: Journalpost): List<DokumentReferanse.VedleggReferanse> {
        return if (journalpost.dokumenter?.size ?: 0 > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun harTilgangTilArkivvariant(dokumentInfo: DokumentInfo?): Boolean =
        dokumentInfo?.dokumentvarianter?.any { dv ->
            dv.variantformat == Variantformat.ARKIV && dv.saksbehandlerHarTilgang
        } == true

}
