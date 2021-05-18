package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.klage.MottakHjemmel
import no.nav.klage.oppgave.domain.kodeverk.*
import java.time.LocalDate

data class KvalitetsvurderingEoesInput(
    val eoes: Eoes?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingRaadfoertMedLegeInput(
    val raadfoertMedLege: RaadfoertMedLege?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingInternVurderingInput(
    val internVurdering: String?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingSendTilbakemeldingInput(
    val sendTilbakemelding: Boolean?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingTilbakemeldingInput(
    val tilbakemelding: String?,
    val klagebehandlingVersjon: Long
)

data class KvalitetsvurderingManuellInput(
    val tema: Tema,
    val foersteinstansEnhet: String,
    val foersteinstansSaksbehandler: String,
    val tildeltKlageenhet: String?,
    val foedselsnummer: String,
    val hjemler: MutableSet<MottakHjemmel>,
    val utfall: Utfall,
    val grunn: Grunn,
    val vedtaksbrevJournalpostId: String?,
    val eoes: Eoes?,
    val raadfoertMedLege: RaadfoertMedLege?,
    val internVurdering: String?,
    val sendTilbakemelding: Boolean?,
    val tilbakemelding: String?,
    val datoRegistrertFoersteinstans: LocalDate,
    val datoMottattNav: LocalDate,
    val datoMottattKlageinstans: LocalDate,
    val datoFullfoertKlageinstans: LocalDate
)
