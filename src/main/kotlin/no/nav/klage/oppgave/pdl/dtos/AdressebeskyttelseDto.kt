package no.nav.klage.oppgave.pdl.dtos

data class AdressebeskyttelseDto(
    val gradering: GraderingDto,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto
)

enum class GraderingDto {
    STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG, UGRADERT
}
