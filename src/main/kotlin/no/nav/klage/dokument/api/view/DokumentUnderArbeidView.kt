package no.nav.klage.dokument.api.view

import java.time.LocalDateTime
import java.util.*

interface DokumentView {
    val id: UUID
    val tittel: String
    val dokumentTypeId: String
    val opplastet: LocalDateTime
    val isSmartDokument: Boolean
    val isMarkertAvsluttet: Boolean
}

data class HovedDokumentView(
    override val id: UUID,
    override val tittel: String,
    override val dokumentTypeId: String,
    override val opplastet: LocalDateTime,
    override val isSmartDokument: Boolean,
    override val isMarkertAvsluttet: Boolean,
    val vedlegg: List<VedleggView>,
) : DokumentView

data class VedleggView(
    override val id: UUID,
    override val tittel: String,
    override val dokumentTypeId: String,
    override val opplastet: LocalDateTime,
    override val isSmartDokument: Boolean,
    override val isMarkertAvsluttet: Boolean,
) : DokumentView