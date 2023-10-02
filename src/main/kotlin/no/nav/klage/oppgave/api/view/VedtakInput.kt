package no.nav.klage.oppgave.api.view

data class VedtakUtfallInput(
    val utfallId: String?,
)

data class VedtakUtfallSetInput(
    val utfallIdSet: Set<String>,
)

data class VedtakHjemlerInput(
    val hjemler: Set<String>?,
    val hjemmelIdSet: Set<String>?,
)