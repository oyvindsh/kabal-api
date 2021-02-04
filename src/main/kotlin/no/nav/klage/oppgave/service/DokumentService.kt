package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.DokumentoversiktBruker
import no.nav.klage.oppgave.clients.saf.graphql.Dokumentvariant
import no.nav.klage.oppgave.clients.saf.graphql.Journalpost
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.SaksdokumentRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafGraphQlClient,
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val saksdokumentRepository: SaksdokumentRepository
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
        val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
        val valgteJournalpostIder =
            saksdokumentRepository.findByKlagebehandlingId(klagebehandlingId).map { it.referanse }.toHashSet()
        val dokumentoversiktBruker: DokumentoversiktBruker =
            safGraphQlClient.getDokumentoversiktBruker(klagebehandling.foedselsnummer, pageSize, previousPageRef)
        return DokumenterResponse(
            dokumenter = dokumentoversiktBruker.journalposter.map { journalpost ->
                dokumentMapper.mapJournalpost(
                    journalpost,
                    valgteJournalpostIder.contains(journalpost.journalpostId)
                )
            },
            pageReference = if (dokumentoversiktBruker.sideInfo.finnesNesteSide) {
                dokumentoversiktBruker.sideInfo.sluttpeker
            } else {
                null
            }
        )
    }

    fun fetchJournalpostIderConnectedToKlagebehandling(
        klagebehandlingId: UUID
    ): List<String> = saksdokumentRepository.findByKlagebehandlingId(klagebehandlingId).map { it.referanse }

    fun fetchJournalposterConnectedToKlagebehandling(
        klagebehandlingId: UUID
    ): DokumenterResponse {
        val saksdokumenter = saksdokumentRepository.findByKlagebehandlingId(klagebehandlingId)
        return saksdokumenter
            .mapNotNull { safGraphQlClient.getJournalpost(it.referanse) }
            .map { dokumentMapper.mapJournalpost(it, true) }
            .let { DokumenterResponse(dokumenter = it, pageReference = null) }
    }

    fun connectJournalpostToKlagebehandling(klagebehandlingId: UUID, journalpostId: String) {
        try {
            if (saksdokumentRepository.existsByKlagebehandlingIdAndReferanse(klagebehandlingId, journalpostId)) {
                logger.debug("Journalpost $journalpostId is already connected to klagebehandling $klagebehandlingId, doing nothing")
            } else {
                saksdokumentRepository.save(
                    Saksdokument(
                        klagebehandlingId = klagebehandlingId,
                        referanse = journalpostId
                    )
                )
            }
        } catch (e: Exception) {
            logger.error("Error connecting $journalpostId to $klagebehandlingId", e)
            throw e
        }
    }

    fun deconnectJournalpostFromKlagebehandling(klagebehandlingId: UUID, journalpostId: String) {
        try {
            val saksdokument =
                saksdokumentRepository.findByKlagebehandlingIdAndReferanse(klagebehandlingId, journalpostId)
            if (saksdokument != null) {
                saksdokumentRepository.delete(saksdokument)
            }
        } catch (e: Exception) {
            logger.error("Error deconnecting $journalpostId from $klagebehandlingId", e)
            throw e
        }
    }

}

class DokumentMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Har ikke tatt høyde for skjerming, ref https://confluence.adeo.no/pages/viewpage.action?pageId=320364687
    fun mapJournalpost(journalpost: Journalpost, isConnected: Boolean) = DokumentReferanse(
        tittel = journalpost.tittel ?: "journalposttittel mangler",
        beskrivelse = journalpost.dokumenter?.map { dokumentinfo -> dokumentinfo.tittel }
            ?.joinToString() ?: "tittel mangler",
        beskrivelser = journalpost.dokumenter?.map { dokumentinfo -> dokumentinfo.tittel.nullAsTittelMangler() }
            ?: emptyList(),
        tema = journalpost.temanavn ?: "tema mangler",
        registrert = journalpost.datoOpprettet.toLocalDate(),
        dokumentInfoId = journalpost.dokumenter?.map { dokumentinfo -> dokumentinfo.dokumentInfoId }
            ?.joinToString() ?: "-",
        journalpostId = journalpost.journalpostId,
        variantFormat = journalpost.dokumenter?.firstOrNull()?.dokumentvarianter?.map { dokumentvariant ->
            logVariantFormat(
                dokumentvariant
            ); dokumentvariant.variantformat.name
        }?.joinToString() ?: "-",
        valgt = isConnected
    )

    private fun String?.nullAsTittelMangler() = this ?: "tittel mangler"

    private fun logVariantFormat(dokumentvariant: Dokumentvariant) {
        logger.debug("Fant variantformat ${dokumentvariant.variantformat} med filnavn ${dokumentvariant.filnavn} og skjerming ${dokumentvariant.skjerming}. Saksbehandler har tilgang? ${dokumentvariant.saksbehandlerHarTilgang}")
    }
}




