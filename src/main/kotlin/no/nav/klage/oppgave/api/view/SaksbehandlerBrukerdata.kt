package no.nav.klage.oppgave.api.view

data class SaksbehandlerView(
    val info: PersonligInfoView,
    val roller: List<String>,
    val enheter: List<EnhetView>,
    val valgtEnhetView: EnhetView,
    val innstillinger: InnstillingerView
) {
    data class PersonligInfoView(
        private val navIdent: String,
        private val azureId: String,
        private val fornavn: String,
        private val etternavn: String,
        private val sammensattNavn: String,
        private val epost: String
    )

    data class InnstillingerView(
        val hjemler: List<String>,
        val temaer: List<String>,
        val typer: List<String>
    )

}