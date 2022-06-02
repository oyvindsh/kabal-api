package no.nav.klage.dokument.api.view

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

data class SmartHovedDokumentInput(
    val content: JsonNode?,
    val templateId: String?,
    val tittel: String?,
    val dokumentTypeId: String? = null,
    val version: Int,
)

data class PatchSmartHovedDokumentInput(
    val content: JsonNode?,
    val templateId: String?,
    val version: Int,
)

data class OptionalPersistentDokumentIdInput(val dokumentId: UUID?)

data class DokumentTitleInput(val title: String)

data class DokumentTypeInput(val dokumentTypeId: String)

data class FerdigstillDokumentInput(val brevmottakertyper: Set<String>?)