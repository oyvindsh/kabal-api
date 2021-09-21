package no.nav.klage.oppgave.domain.saksbehandler

data class SaksbehandlerInfo(
    val info: SaksbehandlerPersonligInfo,
    val roller: List<SaksbehandlerRolle>,
    val enheter: EnheterMedLovligeTemaer,
    val valgtEnhet: EnhetMedLovligeTemaer,
    val innstillinger: SaksbehandlerInnstillinger
)