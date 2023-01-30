package no.nav.klage.oppgave.api.view

import java.time.LocalDate
import java.time.LocalDateTime

data class DokumenterResponse(
    val dokumenter: List<DokumentReferanse>,
    val pageReference: String? = null,
    val antall: Int,
    val totaltAntall: Int
)

data class DokumentReferanse(
    val journalpostId: String,
    val dokumentInfoId: String,
    val tittel: String?,
    val tema: String?,
    val registrert: LocalDate,
    val harTilgangTilArkivvariant: Boolean,
    val valgt: Boolean,
    val vedlegg: MutableList<VedleggReferanse> = mutableListOf(),
    val journalposttype: Journalposttype?,
    val journalstatus: String?,
    val behandlingstema: String?,
    val behandlingstemanavn: String?,
    val sak: Sak?,
    val avsenderMottaker: AvsenderMottaker?,
    val journalfoerendeEnhet: String?,
    val journalfortAvNavn: String?,
    val opprettetAvNavn: String?,
    val datoOpprettet: LocalDateTime,
    val relevantDates: List<RelevantDate>?,
    val antallRetur: Int?,
    val tilleggsopplysninger: List<Tilleggsopplysning>?,
    val kanal: String,
    val utsendingsinfo: Utsendingsinfo?,
) {

    data class AvsenderMottaker(
        val id: String?,
        val type: AvsenderMottakerIdType?,
        val navn: String,
        val land: String?,
        val erLikBruker: Boolean,

        ) {
        enum class AvsenderMottakerIdType {
            //TODO look into NULL
            FNR, ORGNR, HPRNR, UTL_ORG, UKJENT, NULL
        }
    }

    data class VedleggReferanse(
        val dokumentInfoId: String,
        val tittel: String?,
        val harTilgangTilArkivvariant: Boolean,
        val valgt: Boolean
    )

    enum class Journalposttype {
        I, //Inngående dokument: Dokumentasjon som NAV har mottatt fra en ekstern part. De fleste inngående dokumenter er søknader, ettersendelser av dokumentasjon til sak, eller innsendinger fra arbeidsgivere. Meldinger brukere har sendt til "Skriv til NAV" arkiveres også som inngående dokumenter.
        U, //Utgående dokument: Dokumentasjon som NAV har produsert og sendt ut til en ekstern part. De fleste utgående dokumenter er informasjons- eller vedtaksbrev til privatpersoner eller organisasjoner. "Skriv til NAV"-meldinger som saksbehandlere har sendt til brukere arkiveres også som utgående dokumenter.
        N //Notat: Dokumentasjon som NAV har produsert selv, uten at formålet er å distribuere dette ut av NAV. Eksempler på notater er samtalereferater med veileder på kontaktsenter og interne forvaltningsnotater.
    }

    data class Sak(val datoOpprettet: LocalDateTime?, val fagsakId: String?, val fagsaksystem: String?)

    data class RelevantDate(
        val date: LocalDateTime,
        val dateType: DateType,
    ) {
        enum class DateType {
            DATO_SENDT_PRINT,
            DATO_EKSPEDERT,
            DATO_JOURNALFOERT,
            DATO_REGISTRERT,
            DATO_AVS_RETUR,
            DATO_DOKUMENT,
            DATO_LEST,
        }
    }

    data class Tilleggsopplysning(
        val key: String,
        val value: String,
    )

    data class Utsendingsinfo(
        val epostVarselSendt: EpostVarselSendt?,
        val smsVarselSendt: SmsVarselSendt?,
        val fysiskpostSendt: FysiskpostSendt?,
        val digitalpostSendt: DigitalpostSendt?,
    ) {
        data class EpostVarselSendt(
            val tittel: String,
            val adresse: String,
            val varslingstekst: String,
        )

        data class SmsVarselSendt(
            val adresse: String,
            val varslingstekst: String,
        )

        data class FysiskpostSendt(
            val adressetekstKonvolutt: String,
        )

        data class DigitalpostSendt(
            val adresse: String,
        )
    }

}

data class TilknyttetDokument(val journalpostId: String, val dokumentInfoId: String)
