package no.nav.klage.oppgave.domain.klage

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.Registreringshjemmel
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.kafka.ExternalUtfall
import java.time.LocalDateTime

data class AnkeITrygderettenbehandlingInput(
    val klager: Klager,
    val sakenGjelder: SakenGjelder? = null,
    val ytelse: Ytelse,
    val type: Type,
    val kildeReferanse: String,
    val dvhReferanse: String?,
    val sakFagsystem: Fagsystem,
    val sakFagsakId: String? = null,
    val sakMottattKlageinstans: LocalDateTime,
    val saksdokumenter: MutableSet<Saksdokument>,
    val innsendingsHjemler: Set<Hjemmel>?,
    val sendtTilTrygderetten: LocalDateTime,
    val registreringsHjemmelSet: Set<Registreringshjemmel>? = null,
    val ankebehandlingUtfall: ExternalUtfall,
)

fun Behandling.createAnkeITrygderettenbehandlingInput(sendtTilTrygderetten: LocalDateTime? = null): AnkeITrygderettenbehandlingInput {
    return AnkeITrygderettenbehandlingInput(
        klager = klager,
        sakenGjelder = sakenGjelder,
        ytelse = ytelse,
        type = Type.ANKE_I_TRYGDERETTEN,
        kildeReferanse = kildeReferanse,
        dvhReferanse = dvhReferanse,
        sakFagsystem = sakFagsystem,
        sakFagsakId = sakFagsakId,
        sakMottattKlageinstans = mottattKlageinstans,
        saksdokumenter = saksdokumenter,
        innsendingsHjemler = hjemler,
        sendtTilTrygderetten = sendtTilTrygderetten ?: LocalDateTime.now(),
        registreringsHjemmelSet = currentDelbehandling().hjemler,
        ankebehandlingUtfall = ExternalUtfall.valueOf(currentDelbehandling().utfall!!.name),
    )
}
