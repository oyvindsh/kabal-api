package no.nav.klage.oppgave.clients.kabaldocument.model.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.klage.oppgave.domain.klage.PartId
import java.util.*

data class DokumentEnhetFullfoerOutput(
    val brevMottakerWithJoarkAndDokDistInfoList: List<BrevmottakerWithJoarkAndDokDistInfo>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class BrevmottakerWithJoarkAndDokDistInfo(
    val partId: PartId,
    val navn: String?,
    val journalpostId: JournalpostId,
    val dokdistReferanse: UUID?
)

data class JournalpostId(val value: String)
