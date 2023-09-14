package no.nav.klage.oppgave.clients.kabaldocument.model.request

import java.time.LocalDateTime

data class DokumentEnhetWithDokumentreferanserInput(
    val brevMottakere: List<BrevmottakerInput>,
    val journalfoeringData: JournalfoeringDataInput,
    val dokumentreferanser: DokumentInput,
    val dokumentTypeId: String,
    val journalfoerendeSaksbehandlerIdent: String,
) {
    data class DokumentInput(
        val hoveddokument: Dokument,
        val vedlegg: List<Dokument>?,
        val journalfoerteVedlegg: List<JournalfoertDokument>?,
    ) {
        data class Dokument(
            val mellomlagerId: String,
            val opplastet: LocalDateTime,
            val size: Long,
            val name: String,
            val index: Int,
        )

        data class JournalfoertDokument(
            val kildeJournalpostId: String,
            val dokumentInfoId: String,
            val index: Int,
        )
    }
}
