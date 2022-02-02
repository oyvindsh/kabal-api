package no.nav.klage.dokument.api.controller

import java.util.*

data class HovedDokumentInput(val dokumentType: String, val eksternReferanse: UUID)

data class HovedDokumentView(
    val dokumentType: String
)
