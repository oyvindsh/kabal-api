package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.Fagsystem
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
    val sakFagsystem: Fagsystem,
    val klageBehandlendeEnhet: String
)

