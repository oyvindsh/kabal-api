package no.nav.klage.oppgave.domain.elasticsearch

data class RelatedKlagebehandlinger(
    val aapneByFnr: List<EsKlagebehandling>,
    val avsluttedeByFnr: List<EsKlagebehandling>,
    val aapneBySaksreferanse: List<EsKlagebehandling>,
    val avsluttedeBySaksreferanse: List<EsKlagebehandling>,
    val aapneByJournalpostid: List<EsKlagebehandling>,
    val avsluttedeByJournalpostid: List<EsKlagebehandling>
)
