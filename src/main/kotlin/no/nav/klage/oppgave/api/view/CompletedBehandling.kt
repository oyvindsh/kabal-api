package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.Tema
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Utfall
import no.nav.klage.kodeverk.Ytelse
import java.time.LocalDateTime
import java.util.*

data class CompletedBehandling(
    val type: Type,
    val internalSaksnummer: UUID,
    val tema: Tema,
    val ytelse: Ytelse,
    val utfall: Utfall,
    val vedtakDate: LocalDateTime,
    val foedselsnummer: String
)

