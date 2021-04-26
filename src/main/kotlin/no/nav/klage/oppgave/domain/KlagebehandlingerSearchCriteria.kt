package no.nav.klage.oppgave.domain

import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import java.time.LocalDate
import java.time.LocalDateTime

data class KlagebehandlingerSearchCriteria(
    val typer: List<Type> = emptyList(),
    val temaer: List<Tema> = emptyList(),
    val hjemler: List<Hjemmel> = emptyList(),
    val statuskategori: Statuskategori = Statuskategori.AAPEN,

    val opprettetFom: LocalDateTime? = null,
    val opprettetTom: LocalDateTime? = null,
    val ferdigstiltFom: LocalDateTime? = null,
    val ferdigstiltTom: LocalDateTime? = null,
    val fristFom: LocalDate? = null,
    val fristTom: LocalDate? = null,

    val order: Order? = null,
    val offset: Int,
    val limit: Int,
    val erTildeltSaksbehandler: Boolean? = null,
    val saksbehandler: String? = null,
    val enhetsnr: String? = null,
    val projection: Projection? = null,
    val sortField: SortField? = null
) {

    enum class SortField {
        FRIST, MOTTATT
    }

    enum class Order {
        ASC, DESC
    }

    enum class Statuskategori {
        AAPEN, AVSLUTTET
    }


    enum class Projection {
        UTVIDET
    }

    fun isProjectionUtvidet(): Boolean = Projection.UTVIDET == projection

}