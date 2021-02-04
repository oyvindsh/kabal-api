package no.nav.klage.oppgave.api.view

import java.time.LocalDate

data class DokumenterResponse(val dokumenter: List<DokumentReferanse>, val pageReference: String? = null)

data class DokumentReferanserResponse(val journalpostIder: List<String>)

data class DokumentReferanse(
    val tittel: String,
    val beskrivelse: String,
    val beskrivelser: List<String>,
    val tema: String,
    val registrert: LocalDate,
    val dokumentInfoId: String,
    val journalpostId: String,
    val variantFormat: String,
    val valgt: Boolean
)

data class DokumentKnytning(val journalpostId: String)
