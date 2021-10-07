package no.nav.klage.oppgave.api.view

import org.springframework.web.multipart.MultipartFile

data class VedtakVedleggInput(
    val vedlegg: MultipartFile,
    val klagebehandlingVersjon: Long
)

data class VedtakFullfoerInput(
    val journalfoerendeEnhet: String,
    val klagebehandlingVersjon: Long
)

data class VedtakSlettVedleggInput(
    val klagebehandlingVersjon: Long
)

data class VedtakUtfallInput(
    val utfall: String?
)

data class VedtakHjemlerInput(
    val hjemler: Set<String>?
)