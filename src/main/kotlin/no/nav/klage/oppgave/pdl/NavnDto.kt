package no.nav.klage.oppgave.pdl

import java.time.LocalDate

data class NavnDto(
    val fornavn: String,
    val mellomnavn: String,
    val etternavn: String,
    val forkortetNavn: String,
    val originaltNavn: OriginaltNavn,
    val gyldigFraOgMed: LocalDate,
    val folkeregistermetadata: FolkeregistermetadataDto,
    val metadata: MetadataDto
)

data class OriginaltNavn(
    val fornavn: String,
    val mellomnavn: String,
    val etternavn: String
)