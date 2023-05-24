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
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderViewOld,
    val klager: BehandlingDetaljerView.KlagerViewOld,
    val fullmektig: BehandlingDetaljerView.ProsessfullmektigViewOld?,
    val tilknyttedeDokumenter: List<TilknyttetDokument>,
    val sakFagsakId: String,
    val fagsakId: String,
    val sakFagsystem: Fagsystem,
    val fagsystem: Fagsystem,
    val fagsystemId: String,
    val klageBehandlendeEnhet: String,
    val tildeltSaksbehandlerIdent: String?,
    val tildeltSaksbehandlerNavn: String?,
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
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderViewOld,
    val klager: BehandlingDetaljerView.KlagerViewOld,
    val fullmektig: BehandlingDetaljerView.ProsessfullmektigViewOld?,
    val tilknyttedeDokumenter: List<TilknyttetDokument>,
    val mottattNav: LocalDate,
    val frist: LocalDate,
    val sakFagsakId: String,
    val fagsakId: String,
    val sakFagsystem: Fagsystem,
    val fagsystem: Fagsystem,
    val fagsystemId: String,
    val journalpost: DokumentReferanse,
    val tildeltSaksbehandler: TildeltSaksbehandler?,
)

data class CreatedKlagebehandlingStatusForKabin(
    val typeId: String,
    val behandlingId: UUID,
    val ytelseId: String,
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderViewOld,
    val klager: BehandlingDetaljerView.KlagerViewOld,
    val fullmektig: BehandlingDetaljerView.ProsessfullmektigViewOld?,
    val mottattVedtaksinstans: LocalDate,
    val mottattKlageinstans: LocalDate,
    val frist: LocalDate,
    val fagsakId: String,
    val fagsystemId: String,
    val journalpost: DokumentReferanse,
    val kildereferanse: String,
    val tildeltSaksbehandler: TildeltSaksbehandler?,
)

data class TildeltSaksbehandler(
    val navIdent: String,
    val navn: String,
)