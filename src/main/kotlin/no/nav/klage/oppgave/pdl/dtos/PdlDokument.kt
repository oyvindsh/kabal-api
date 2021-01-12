package no.nav.klage.oppgave.pdl.dtos

data class PdlDokument(
    val hentPerson: PersonDto,
    val hentIdenter: HentIdenterDto
)