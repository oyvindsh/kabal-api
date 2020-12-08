package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import java.time.LocalDateTime

data class OppgaveKopi(

    val id: Long,
    val versjon: Int,
    val journalpostId: String? = null,
    val saksreferanse: String? = null,
    val mappeId: Long? = null,
    val status: Status,
    val tildeltEnhetsnr: String,
    val opprettetAvEnhetsnr: String? = null,
    val endretAvEnhetsnr: String? = null,
    val tema: String,
    val temagruppe: String? = null,
    val behandlingstema: String? = null,
    val oppgavetype: String,
    val behandlingstype: String? = null,
    val prioritet: Prioritet,
    val tilordnetRessurs: String? = null,
    val beskrivelse: String? = null,
    val fristFerdigstillelse: LocalDate,
    val aktivDato: LocalDate,
    val opprettetAv: String,
    val endretAv: String? = null,
    val opprettetTidspunkt: LocalDateTime,
    val endretTidspunkt: LocalDateTime? = null,
    val ferdigstiltTidspunkt: LocalDateTime? = null,
    val behandlesAvApplikasjon: String? = null,
    val journalpostkilde: String? = null,
    val ident: Ident? = null,
    val metadata: Map<MetadataNoekkel, String>? = null
) {
    fun statuskategori(): Statuskategori = status.kategoriForStatus()
}