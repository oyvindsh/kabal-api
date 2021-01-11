package no.nav.klage.oppgave.pdl

data class IdentDetaljDto(
    val ident: String,
    val historisk: Boolean,
    val gruppe: IdentGruppeDto
) {

    companion object {
        fun mapIdentGruppe(identGruppe: String): IdentGruppeDto {
            return when (identGruppe.toUpperCase()) {
                "AKTOR_ID" -> IdentGruppeDto.AKTORID
                "FOLKEREGISTERIDENT" -> IdentGruppeDto.FOLKEREGISTERIDENT
                "N_PID" -> IdentGruppeDto.NPID
                else -> throw RuntimeException("Unable to map $identGruppe to IdentGruppe")
            }
        }
    }
}

enum class IdentGruppeDto {
    AKTORID, FOLKEREGISTERIDENT, NPID
}
