package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.DokumentoversiktBruker
import no.nav.klage.oppgave.clients.saf.graphql.Dokumentvariant
import no.nav.klage.oppgave.clients.saf.graphql.Journalpost
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.clients.saf.rest.SafRestClient
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafRestClient,
    private val klagebehandlingRepository: KlagebehandlingRepository,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val dokumentMapper = DokumentMapper()
    }

    fun fetchDokumentlisteForKlagebehandling(
        klagebehandlingId: UUID,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        val klagebehandling: Klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)

        if (klagebehandling.foedselsnummer != null) {
            val valgteJournalpostIder =
                klagebehandling.saksdokumenter.map { it.referanse }.toHashSet()
            val dokumentoversiktBruker: DokumentoversiktBruker =
                safGraphQlClient.getDokumentoversiktBruker(klagebehandling.foedselsnummer, pageSize, previousPageRef)

            secureLogger.debug("Journalposter: {}", dokumentoversiktBruker.journalposter)

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
        } else {
            return DokumenterResponse(dokumenter = emptyList(), pageReference = null)
        }
    }

    fun fetchJournalpostIderConnectedToKlagebehandling(klagebehandlingId: UUID): List<String> {
        val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
        return klagebehandling.saksdokumenter.map { it.referanse }
    }

    fun fetchJournalposterConnectedToKlagebehandling(klagebehandlingId: UUID): DokumenterResponse {
        val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
        return klagebehandling.saksdokumenter
            .mapNotNull { safGraphQlClient.getJournalpost(it.referanse) }
            .map { dokumentMapper.mapJournalpost(it, true) }
            .let { DokumenterResponse(dokumenter = it, pageReference = null) }
    }

    fun connectJournalpostToKlagebehandling(klagebehandlingId: UUID, journalpostId: String) {
        val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)

        validateJournalpostExists(journalpostId)

        try {
            if (klagebehandling.saksdokumenter.any { it.referanse == journalpostId }) {
                logger.debug("Journalpost $journalpostId is already connected to klagebehandling $klagebehandlingId, doing nothing")
            } else {
                klagebehandling.saksdokumenter.add(Saksdokument(referanse = journalpostId))
            }
        } catch (e: Exception) {
            logger.error("Error connecting $journalpostId to $klagebehandlingId", e)
            throw e
        }
    }

    fun disconnectJournalpostFromKlagebehandling(klagebehandlingId: UUID, journalpostId: String) {
        val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
        klagebehandling.saksdokumenter.removeIf { it.referanse == journalpostId }
    }

    private fun validateJournalpostExists(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpost(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun getFile(journalpostId: String, dokumentInfoId: String, format: String = "ARKIV"): ArkivertDokument {
        return safRestClient.getDokument(dokumentInfoId, journalpostId, format)
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




