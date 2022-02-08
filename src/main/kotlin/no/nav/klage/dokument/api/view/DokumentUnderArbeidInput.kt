package no.nav.klage.dokument.api.view

import java.util.*

data class HovedDokumentInput(val eksternReferanse: UUID)

data class SmartHovedDokumentInput(val eksternReferanse: UUID, val json: String)

data class PersistentDokumentIdInput(val id: UUID)

data class DokumentTypeInput(val dokumentTypeId: String)