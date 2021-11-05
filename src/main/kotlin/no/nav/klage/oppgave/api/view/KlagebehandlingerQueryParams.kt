package no.nav.klage.oppgave.api.view

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class KlagebehandlingerQueryParams(
    var typer: List<String> = emptyList(),
    var temaer: List<String> = emptyList(),
    var hjemler: List<String> = emptyList(),
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val sortering: Sortering? = Sortering.FRIST,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null,
    val erTildeltSaksbehandler: Boolean? = null,
    val tildeltSaksbehandler: String? = null,
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val ferdigstiltFom: LocalDate? = null,
    val ferdigstiltDaysAgo: Long? = null,
    val enhet: String
) {

    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Sortering {
        FRIST, MOTTATT
    }

    enum class Projeksjon {
        UTVIDET
    }
}
