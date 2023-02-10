package no.nav.klage.oppgave.api.view

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class CreateAnkeBasedOnKabinInput(
    val klagebehandlingId: UUID,
    val mottattNav: LocalDate,
    val klager: OversendtKlager?,
    val prosessfullmektig: OversendtProsessfullmektig?,
    val ankeDocumentJournalpostId: String,
) {
    data class OversendtKlager(
        val id: OversendtPartId,
    )

    data class OversendtProsessfullmektig(
        val id: OversendtPartId,
    )

    data class OversendtPartId(
        val type: OversendtPartIdType,
        val verdi: String
    )

    enum class OversendtPartIdType { PERSON, VIRKSOMHET }
}