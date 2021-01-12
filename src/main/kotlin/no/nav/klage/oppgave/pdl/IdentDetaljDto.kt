package no.nav.klage.oppgave.pdl

data class IdentDetaljDto(
    val ident: String,
    val historisk: Boolean,
    val gruppe: IdentGruppeDto,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto?
)

enum class IdentGruppeDto {
    AKTORID, FOLKEREGISTERIDENT, NPID
}
