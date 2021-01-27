package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.DokumentoversiktBrukerResponse
import no.nav.klage.oppgave.clients.saf.graphql.Dokumentvariant
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.clients.saf.graphql.Variantformat
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafGraphQlClient,
    private val klagebehandlingRepository: KlagebehandlingRepository,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Første dokument er hoveddokument, resten er vedlegg. Må undersøke hva det egentlig er og hvordan det skal vises i lista..
    fun hentDokumentlisteForKlagebehandling(klagebehandlingId: String): DokumenterResponse {
        val klagebehandling = klagebehandlingRepository.getOne(UUID.fromString(klagebehandlingId))
        val safResponse: DokumentoversiktBrukerResponse =
            safGraphQlClient.getDokumentoversiktBruker(klagebehandling.foedselsnummer, 100, null)
        return DokumenterResponse(dokumenter = safResponse.data.dokumentoversiktBruker.journalposter.map {
            DokumentReferanse(
                beskrivelse = it.dokumenter?.firstOrNull()?.tittel ?: "tittel mangler",
                tema = it.temanavn ?: "tema mangler",
                registrert = it.datoOpprettet.toLocalDate(),
                dokumentInfoId = it.dokumenter?.firstOrNull()?.dokumentInfoId ?: "-",
                journalpostId = it.journalpostId,
                variantFormat = it.dokumenter?.firstOrNull()?.dokumentvarianter?.findVariantformatArkiv()?.variantformat?.name
                    ?: "-",
            )
        }, pageReference = safResponse.data.dokumentoversiktBruker.sideInfo.sluttpeker)
    }

    //TODO: Hvis det finnes et sladdet dokument, skal vi velge det? Bør vi filtrere på saksbehandlerHarTilgang, eller evt returnere feltet så det kan vises i GUI? Hva med skjerming?
    private fun List<Dokumentvariant>?.findVariantformatArkiv(): Dokumentvariant? =
        this?.let { logger.debug("Funnet følgende variantformater ${it}"); it }
            ?.find { it.variantformat == Variantformat.ARKIV }

}


