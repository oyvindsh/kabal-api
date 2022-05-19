package no.nav.klage.dokument.api.view

import com.fasterxml.jackson.databind.JsonNode
import java.util.*

data class SmartHovedDokumentInput(
    val json: String?,
    //will replace json property
    val content: JsonNode?,
    val templateId: String?,
    val tittel: String?,
    val dokumentTypeId: String? = null
)

data class OptionalPersistentDokumentIdInput(val dokumentId: UUID?)

data class DokumentTitleInput(val title: String)

data class DokumentTypeInput(val dokumentTypeId: String)

data class FerdigstillDokumentInput(val brevmottakertyper: Set<String>?)