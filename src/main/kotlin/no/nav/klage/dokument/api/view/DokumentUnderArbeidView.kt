package no.nav.klage.dokument.api.view

import java.time.LocalDateTime
import java.util.*

data class DokumentView(
    val id: UUID,
    val tittel: String,
    val dokumentTypeId: String,
    val opplastet: LocalDateTime,
    val isSmartDokument: Boolean,
    val isMarkertAvsluttet: Boolean,
    val parent: UUID?,
)