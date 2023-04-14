package no.nav.klage.oppgave.api.view

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.domain.klage.*
import java.time.LocalDate
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
data class CreateKlageBasedOnKabinInput(
    val sakenGjelder: OversendtPartId,
    val klager: OversendtPartId?,
    val fullmektig: OversendtPartId?,
    val fagsakId: String,
    val fagsystemId: String,
    val hjemmelIdList: List<String>?,
    val forrigeBehandlendeEnhet: String,
    val klageJournalpostId: String,
    val brukersHenvendelseMottattNav: LocalDate,
    val sakMottattKa: LocalDate,
    val frist: LocalDate,
    val ytelseId: String,
    val kildereferanse: String,
) {
    data class OversendtPartId(
        val type: OversendtPartIdType,
        val value: String
    )

    fun OversendtPartId.toPartId(): PartId {
        return PartId(
            type = PartIdType.of(type.name),
            value = value
        )
    }

    enum class OversendtPartIdType { PERSON, VIRKSOMHET }
}

fun CreateKlageBasedOnKabinInput.toMottak(forrigeBehandlingId: UUID? = null): Mottak {
    val prosessfullmektig = if (fullmektig != null) {
        Prosessfullmektig(
            partId = fullmektig.toPartId(),
            skalPartenMottaKopi = true
        )
    } else {
        null
    }

    val klager = if (klager != null) {
        Klager(
            partId = klager.toPartId(),
            prosessfullmektig = prosessfullmektig
        )
    } else {
        Klager(
            partId = sakenGjelder.toPartId(),
            prosessfullmektig = prosessfullmektig
        )
    }

    return Mottak(
        type = Type.KLAGE,
        klager = klager,
        sakenGjelder = SakenGjelder(
            partId = sakenGjelder.toPartId(),
            //TODO ever used?
            skalMottaKopi = false
        ),
        innsynUrl = null,
        fagsystem = Fagsystem.of(fagsystemId),
        fagsakId = fagsakId,
        kildeReferanse = kildereferanse,
        dvhReferanse = null,
        hjemler = hjemmelIdList?.map { MottakHjemmel(hjemmelId = it) }?.toSet(),
        forrigeBehandlendeEnhet = forrigeBehandlendeEnhet,
        mottakDokument = mutableSetOf(
            MottakDokument(
                type = MottakDokumentType.BRUKERS_KLAGE,
                journalpostId = klageJournalpostId
            )
        ),
        innsendtDato = null,
        brukersHenvendelseMottattNavDato = brukersHenvendelseMottattNav,
        sakMottattKaDato = sakMottattKa.atStartOfDay(),
        frist = frist,
        ytelse = Ytelse.of(ytelseId),
        forrigeBehandlingId = forrigeBehandlingId,
        sentFrom = Mottak.Sender.KABIN,
    )
}

data class CreatedKlageResponse(
    val mottakId: UUID,
)