package no.nav.klage.oppgave.domain.trygderetten

import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun toXml(arkivmelding: Arkivmelding): String {
    return xml("avtalemelding") {
        xmlns = "http://www.arkivverket.no/standarder/noark5/arkivmelding"
        namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")
        namespace("xsi:schemaLocation", "http://www.arkivverket.no/standarder/noark5/arkivmelding arkivmelding.xsd")
        "system" { -arkivmelding.system }
        "meldingsId" { -arkivmelding.meldingId }
        "tidspunkt" { -arkivmelding.tidspunkt.truncatedToSeconds() }
        "antallFiler" { -arkivmelding.antallFiler.toString() }
        "mappe" {
            attribute("xsi:type", "saksmappe")
            "tittel" { -arkivmelding.mappe.tittel }
            "opprettetDato" { -arkivmelding.mappe.opprettetDato.truncatedToSeconds() }
            "virksomhetsspesifikkeMetadata" { -arkivmelding.mappe.virksomhetsspesifikkeMetadata }
            "part" {
                "partNavn" { -arkivmelding.mappe.part.partNavn }
                "partRolle" { -arkivmelding.mappe.part.partRolle }
                arkivmelding.mappe.part.organisasjonsnummer?.organisasjonsnummer?.let {
                    "organisasjonsnummer" {
                        "organisasjonsnummer" { -arkivmelding.mappe.part.organisasjonsnummer.organisasjonsnummer }
                    }
                }
                arkivmelding.mappe.part.foedselsnummer?.foedselsnummer?.let {
                    "foedselsnummer" {
                        "foedselsnummer" { -arkivmelding.mappe.part.foedselsnummer.foedselsnummer }
                    }
                }
                arkivmelding.mappe.part.kontaktperson?.let {
                    "kontaktperson" { -arkivmelding.mappe.part.kontaktperson }
                }
            }
            "registrering" {
                attribute("xsi:type", "journalpost")
                "opprettetDato" { -arkivmelding.mappe.registrering.opprettetDato.truncatedToSeconds() }
                "opprettetAv" { -arkivmelding.mappe.registrering.opprettetAv }
                "dokumentbeskrivelse" {
                    "dokumenttype" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumenttype }
                    "dokumentstatus" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentstatus }
                    "tittel" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.tittel }
                    "opprettetDato" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.opprettetDato.truncatedToSeconds() }
                    "opprettetAv" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.opprettetAv }
                    "tilknyttetRegistreringSom" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.tilknyttetRegistreringSom }
                    "dokumentnummer" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentnummer }
                    "tilknyttetDato" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.tilknyttetDato.truncatedToSeconds() }
                    "tilknyttetAv" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.tilknyttetAv }
                    "dokumentobjekt" {
                        "versjonsnummer" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentobjekt.versjonsnummer }
                        "variantformat" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentobjekt.variantformat }
                        "opprettetDato" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentobjekt.opprettetDato.truncatedToSeconds() }
                        "opprettetAv" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentobjekt.opprettetAv }
                        "referanseDokumentfil" { -arkivmelding.mappe.registrering.dokumentbeskrivelse.dokumentobjekt.referanseDokumentfil }
                    }
                }
                "tittel" { -arkivmelding.mappe.registrering.tittel }
                "korrespondansepart" {
                    "korrespondanseparttype" { -arkivmelding.mappe.registrering.korrespondansepart.korrespondanseparttype }
                    "korrespondansepartNavn" { -arkivmelding.mappe.registrering.korrespondansepart.korrespondansepartNavn }
                    "organisasjonsummer" {
                        "organisasjonsummer" { -arkivmelding.mappe.registrering.korrespondansepart.organisasjonsnummer.organisasjonsnummer }
                    }
                }
                "journalposttype" { -arkivmelding.mappe.registrering.journalposttype }
                "journalstatus" { -arkivmelding.mappe.registrering.journalstatus }
                "journaldato" { -arkivmelding.mappe.registrering.journaldato.toString() }
            }
            "saksdato" { -arkivmelding.mappe.saksdato.toString() }
            "administrativEnhet" { -arkivmelding.mappe.administrativEnhet }
            "saksansvarlig" { -arkivmelding.mappe.saksansvarlig }
            "journalenhet" { -arkivmelding.mappe.journalenhet }
            "saksstatus" { -arkivmelding.mappe.saksstatus }
        }
    }.toString(PrintOptions(singleLineTextElements = true))
}

private fun LocalDateTime.truncatedToSeconds() = this.truncatedTo(ChronoUnit.SECONDS).toString()

class Arkivmelding(
    val system: String,
    val meldingId: String,
    val tidspunkt: LocalDateTime,
    val antallFiler: Int,
    val mappe: Mappe
) {

    data class Mappe(
        val tittel: String,
        val opprettetDato: LocalDateTime,
        val virksomhetsspesifikkeMetadata: String,
        val part: Part,
        val registrering: Registrering,
        val saksdato: LocalDate,
        val administrativEnhet: String,
        val saksansvarlig: String,
        val journalenhet: String,
        val saksstatus: String

    ) {
        data class Part(
            val partNavn: String,
            val partRolle: String,
            val organisasjonsnummer: Organisasjonsnummer?,
            val foedselsnummer: Foedselsnummer?,
            val kontaktperson: String?
        ) {

            data class Foedselsnummer(
                val foedselsnummer: String
            )
        }

        data class Registrering(
            val opprettetDato: LocalDateTime,
            val opprettetAv: String,
            val dokumentbeskrivelse: Dokumentbeskrivelse,
            val tittel: String,
            val korrespondansepart: Korrespondansepart,
            val journalposttype: String,
            val journalstatus: String,
            val journaldato: LocalDate

        ) {
            data class Dokumentbeskrivelse(
                val dokumenttype: String,
                val dokumentstatus: String,
                val tittel: String,
                val opprettetDato: LocalDateTime,
                val opprettetAv: String,
                val tilknyttetRegistreringSom: String,
                val dokumentnummer: String,
                val tilknyttetDato: LocalDateTime,
                val tilknyttetAv: String,
                val dokumentobjekt: Dokumentobjekt

            ) {
                data class Dokumentobjekt(
                    val versjonsnummer: String,
                    val variantformat: String,
                    val opprettetDato: LocalDateTime,
                    val opprettetAv: String,
                    val referanseDokumentfil: String
                )
            }

            data class Korrespondansepart(
                val korrespondanseparttype: String,
                val korrespondansepartNavn: String,
                val organisasjonsnummer: Organisasjonsnummer

            )
        }
    }

    data class Organisasjonsnummer(
        val organisasjonsnummer: String
    )
}

