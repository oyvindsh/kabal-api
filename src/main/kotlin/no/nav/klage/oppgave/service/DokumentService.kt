package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.DokumentoversiktBruker
import no.nav.klage.oppgave.clients.saf.graphql.Dokumentvariant
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
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
    }

    /*
    fun fetchDokumenterConnectedToKlagebehandling(klagebehandlingId: UUID, includeMetadata: Boolean) {

        val connectedDokumenter = saksdokumentRepository.findByKlagebehandlingId(klagebehandlingId)
        val enrichedConnectedDokumenter = if (includeMetadata) {
            connectedDokumenter.map { safGraphQlClient.getDokumentoversiktBruker() }
        }
    }
    */

    //TODO: Første dokument er hoveddokument, resten er vedlegg. Må undersøke hvordan det skal vises i lista..
    fun fetchDokumentlisteForKlagebehandling(klagebehandlingId: UUID): DokumenterResponse {
        val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
        val dokumentoversiktBruker: DokumentoversiktBruker =
            safGraphQlClient.getDokumentoversiktBruker(klagebehandling.foedselsnummer, 100, null)
        return DokumenterResponse(dokumenter = dokumentoversiktBruker.journalposter.map { journalpost ->
            DokumentReferanse(
                tittel = journalpost.tittel ?: "journalposttittel mangler",
                beskrivelse = journalpost.dokumenter?.map { dokumentinfo -> dokumentinfo.tittel }
                    ?.joinToString() ?: "tittel mangler",
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
            )
        }, pageReference = dokumentoversiktBruker.sideInfo.sluttpeker)
    }

    fun logVariantFormat(dokumentvariant: Dokumentvariant) {
        logger.debug("Fant variantformat ${dokumentvariant.variantformat} med filnavn ${dokumentvariant.filnavn} og skjerming ${dokumentvariant.skjerming}. Saksbehandler har tilgang? ${dokumentvariant.saksbehandlerHarTilgang}")
    }
}


