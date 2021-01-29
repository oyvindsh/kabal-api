package no.nav.klage.oppgave.api.internal

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class OppgaveKopiAPIModel(
    val id: Long,
    val versjon: Int,
    val journalpostId: String? = null,
    val saksreferanse: String? = null,
    val mappeId: Long? = null,
    val status: Status,
    val statuskategori: Statuskategori?,
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
    val fristFerdigstillelse: LocalDate? = null,
    val aktivDato: LocalDate,
    val opprettetAv: String,
    val endretAv: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    val opprettetTidspunkt: LocalDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    val endretTidspunkt: LocalDateTime? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    val ferdigstiltTidspunkt: LocalDateTime? = null,
    val behandlesAvApplikasjon: String? = null,
    val journalpostkilde: String? = null,
    val ident: Ident,
    val metadata: Map<MetadataKey, String>?
) {
    enum class MetadataKey {
        NORM_DATO, REVURDERINGSTYPE, SOKNAD_ID, KRAV_ID, MOTTATT_DATO, EKSTERN_HENVENDELSE_ID, SKANNET_DATO, RINA_SAKID, HJEMMEL
    }

    data class Ident(
        val id: Long? = null,
        val identType: IdentType,
        val verdi: String,
        val folkeregisterident: String? = null
    )

    enum class IdentType {
        AKTOERID, ORGNR, SAMHANDLERNR, BNR
    }

    enum class Status {
        OPPRETTET, AAPNET, UNDER_BEHANDLING, FERDIGSTILT, FEILREGISTRERT
    }

    enum class Statuskategori {
        AAPEN, AVSLUTTET
    }

    enum class Prioritet {
        HOY, NORM, LAV
    }
}