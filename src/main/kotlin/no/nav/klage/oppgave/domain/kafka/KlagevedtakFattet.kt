package no.nav.klage.oppgave.domain.kafka

data class KlagevedtakFattet (
    val id: String,
    val utfall: KlagevedtakUtfall,
    val vedtaksbrevReferanse: String
)

enum class KlagevedtakUtfall {
    TRUKKET,
    RETUR,
    OPPHEVET,
    MEDHOD,
    DELVIS_MEDHOLD,
    OPPRETTHOLDT,
    UGUNST,
    AVVIST
}
