package no.nav.klage.oppgave.pdl

data class IdentDetaljDto(
    val ident: String,
    val historisk: Boolean,
    val gruppe: IdentGruppeDto
)

enum class IdentGruppeDto {
    AKTORID, FOLKEREGISTERIDENT, NPID
}
