package no.nav.klage.oppgave.clients.kabaldocument

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.oppgave.clients.kabaldocument.model.request.UpdateTitleInput
import no.nav.klage.oppgave.clients.kabaldocument.model.response.BrevmottakerWithJoarkAndDokDistInfo
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class KabalDocumentGateway(
    private val kabalDocumentClient: KabalDocumentClient,
    private val kabalDocumentMapper: KabalDocumentMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun createKomplettDokumentEnhet(
        behandling: Behandling,
        hovedDokument: DokumentUnderArbeid,
        vedlegg: SortedSet<DokumentUnderArbeid>
    ): UUID {
        return UUID.fromString(
            kabalDocumentClient.createDokumentEnhetWithDokumentreferanser(
                kabalDocumentMapper.mapBehandlingToDokumentEnhetWithDokumentreferanser(
                    behandling,
                    hovedDokument,
                    vedlegg
                )
            ).id
        )
    }

    fun fullfoerDokumentEnhet(dokumentEnhetId: UUID): List<BrevmottakerWithJoarkAndDokDistInfo> =
        kabalDocumentClient.fullfoerDokumentEnhet(dokumentEnhetId).brevMottakerWithJoarkAndDokDistInfoList

    fun updateDocumentTitle(
        journalpostId: String,
        dokumentInfoId: String,
        newTitle: String
    ) {
        kabalDocumentClient.updateDocumentTitle(
            input = UpdateTitleInput(
                journalpostId = journalpostId,
                dokumentInfoId = dokumentInfoId,
                newTitle = newTitle
            )
        )
    }
}