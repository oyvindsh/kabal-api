package no.nav.klage.oppgave.pdl

data class KjoennDto(
    val kjoenn: KjoennTypeDto?,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto
)

enum class KjoennTypeDto {
    MANN, KVINNE, UKJENT
}