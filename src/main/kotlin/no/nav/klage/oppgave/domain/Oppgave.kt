package no.nav.klage.oppgave.domain

import java.time.LocalDate


data class OppgaveResponse(
    val antallTreffTotalt: Int,
    val oppgaver: Array<Oppgave>
)

data class Oppgave(
    val id: Int,
    val tildeltEnhetsnr: String?,
    val endretAvEnhetsnr: String?,
    val opprettetAvEnhetsnr: String?,
    val journalpostId: String?,
    val journalpostkilde: String?,
    val behandlesAvApplikasjon: String?,
    val saksreferanse: String?,
    val bnr: String?,
    val samhandlernr: String?,
    val aktoerId: String?,
    val identer: Array<Ident>?,
    val orgnr: String?,
    val tilordnetRessurs: String?,
    val beskrivelse: String?,
    val temagruppe: String?,
    val tema: String,
    val behandlingstema: String?,
    val oppgavetype: String?,
    val behandlingstype: String?,
    val versjon: Int?,
    val mappeId: Long?,
    val opprettetAv: String?,
    val endretAv: String?,
    val prioritet: Prioritet?,
    val status: Status?,
    val metadata: Map<String, String>?,
    val fristFerdigstillelse: LocalDate?,
    val aktivDato: String?,
    val opprettetTidspunkt: String?,
    val ferdigstiltTidspunkt: String?,
    val endretTidspunkt: String?
) {
    data class Ident(
        val ident: String?,
        val gruppe: Gruppe?
    )

    enum class Gruppe {
        FOLKEREGISTERIDENT, AKTOERID, NPID
    }

    enum class Prioritet {
        HOY, NORM, LAV
    }

    enum class Status {
        OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT
    }
}



