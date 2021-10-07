package no.nav.klage.oppgave.api.view

import java.time.LocalDate

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
    val vedlegg: MutableList<VedleggReferanse> = mutableListOf()
) {

    data class VedleggReferanse(
        val dokumentInfoId: String,
        val tittel: String?,
        val harTilgangTilArkivvariant: Boolean,
        val valgt: Boolean
    )
}

data class TilknyttetDokument(val journalpostId: String, val dokumentInfoId: String)
