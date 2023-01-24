package no.nav.klage.oppgave.clients.kabaldocument.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class DokumentEnhetFullfoerOutput(
    val brevMottakerWithJoarkAndDokDistInfoList: List<BrevmottakerWithJoarkAndDokDistInfo>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BrevmottakerWithJoarkAndDokDistInfo(
    val journalpostId: JournalpostId,
)

data class JournalpostId(val value: String)
