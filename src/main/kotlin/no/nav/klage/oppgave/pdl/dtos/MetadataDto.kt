package no.nav.klage.oppgave.pdl.dtos

import java.time.LocalDateTime

data class MetadataDto(
    val opplysningsId: String?,
    val master: String,
    val endringer: List<EndringDto>,
    val historisk: Boolean
)

data class EndringDto(
    val type: String,
    val registrert: LocalDateTime,
    val registrertAv: String,
    val systemkilde: String,
    val kilde: String
)