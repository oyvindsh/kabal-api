package no.nav.klage.oppgave.api.view.kabin

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.oppgave.api.view.BehandlingDetaljerView
import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.TilknyttetDokument
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class CompletedKlagebehandling(
    val behandlingId: UUID,
    val ytelseId: String,
    val utfallId: String,
    val vedtakDate: LocalDateTime,
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderView,
    val klager: BehandlingDetaljerView.KlagerView,
    val fullmektig: BehandlingDetaljerView.ProsessfullmektigView?,
    val tilknyttedeDokumenter: List<TilknyttetDokument>,
    val sakFagsakId: String,
    val fagsakId: String,
    val sakFagsystem: Fagsystem,
    val fagsystem: Fagsystem,
    val fagsystemId: String,
    val klageBehandlendeEnhet: String,
)

data class CreatedAnkeResponse(
    val mottakId: UUID,
)

data class CreatedKlageResponse(
    val mottakId: UUID,
)

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