package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.Utfall
import no.nav.klage.kodeverk.Ytelse
import java.time.LocalDateTime
import java.util.*

data class CompletedKlagebehandling(
    val behandlingId: UUID,
    val ytelse: Ytelse,
    val utfall: Utfall,
    val vedtakDate: LocalDateTime,
    val sakenGjelder: BehandlingDetaljerView.SakenGjelderView,
    val klager: BehandlingDetaljerView.KlagerView,
    val prosessfullmektig: BehandlingDetaljerView.ProsessfullmektigView?,
    val tilknyttedeDokumenter: List<TilknyttetDokument>,
    val saksId: String?
)

