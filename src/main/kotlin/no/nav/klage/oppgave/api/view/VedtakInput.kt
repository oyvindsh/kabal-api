package no.nav.klage.oppgave.api.view

import org.springframework.web.multipart.MultipartFile

data class VedtakVedleggInput(
    val vedlegg: MultipartFile
)

data class VedtakFullfoerInput(
    val journalfoerendeEnhet: String
)

data class VedtakUtfallInput(
    val utfall: String?
)

data class VedtakHjemlerInput(
    val hjemler: Set<String>?
)

data class SmartEditorIdInput(
    val smartEditorId: String?
)