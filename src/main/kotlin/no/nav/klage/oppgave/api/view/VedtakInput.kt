package no.nav.klage.oppgave.api.view

import org.springframework.web.multipart.MultipartFile

data class VedtakUtfallInput(val utfall: String, val klagebehandlingVersjon: Long? = null)

data class VedtakGrunnInput(val grunn: String?, val klagebehandlingVersjon: Long? = null)

data class VedtakHjemlerInput(val hjemler: List<String>?, val klagebehandlingVersjon: Long? = null)

data class VedtakVedleggInput(
    val vedlegg: MultipartFile,
    val journalfoerendeEnhet: String,
    val klagebehandlingVersjon: Long? = null
)

data class VedtakFullfoerInput(val journalfoerendeEnhet: String, val klagebehandlingVersjon: Long? = null)