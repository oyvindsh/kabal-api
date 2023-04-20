package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.Fagsystem
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class CreatedAnkebehandlingStatusForKabin(
    val typeId: String,
    val behandlingId: UUID,
    val ytelseId: String,
    val utfallId: String,
    val vedtakDate: LocalDateTime,
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderView,
    val klager: BehandlingDetaljerView.KlagerView,
    val fullmektig: BehandlingDetaljerView.ProsessfullmektigView?,
    val tilknyttedeDokumenter: List<TilknyttetDokument>,
    val mottattNav: LocalDate,
    val frist: LocalDate,
    val sakFagsakId: String,
    val fagsakId: String,
    val sakFagsystem: Fagsystem,
    val fagsystem: Fagsystem,
    val fagsystemId: String,
    val journalpost: DokumentReferanse,
)

data class CreatedKlagebehandlingStatusForKabin(
    val typeId: String,
    val behandlingId: UUID,
    val ytelseId: String,
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderView,
    val klager: BehandlingDetaljerView.KlagerView,
    val fullmektig: BehandlingDetaljerView.ProsessfullmektigView?,
    val mottattVedtaksinstans: LocalDate,
    val mottattKlageinstans: LocalDate,
    val frist: LocalDate,
    val fagsakId: String,
    val fagsystemId: String,
    val journalpost: DokumentReferanse,
    val kildereferanse: String,
)