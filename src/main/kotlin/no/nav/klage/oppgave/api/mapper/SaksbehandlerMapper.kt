package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.api.view.EnhetView
import no.nav.klage.oppgave.api.view.SaksbehandlerView
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import no.nav.klage.oppgave.domain.saksbehandler.*

fun SaksbehandlerInfo.mapToView() =
    SaksbehandlerView(
        info = info.mapToView(),
        roller = roller.map { it.id },
        enheter = enheter.mapToView(),
        valgtEnhetView = valgtEnhet.mapToView(),
        innstillinger = innstillinger.mapToView()
    )

fun SaksbehandlerPersonligInfo.mapToView() = SaksbehandlerView.PersonligInfoView(
    navIdent = navIdent,
    azureId = azureId,
    fornavn = fornavn,
    etternavn = etternavn,
    sammensattNavn = sammensattNavn,
    epost = epost
)

fun SaksbehandlerInnstillinger.mapToView() = SaksbehandlerView.InnstillingerView(
    hjemler = hjemler.map { it.id },
    temaer = temaer.map { it.id },
    typer = typer.map { it.id }
)

fun SaksbehandlerView.InnstillingerView.mapToDomain() = SaksbehandlerInnstillinger(
    hjemler = hjemler.map { Hjemmel.of(it) },
    temaer = temaer.map { Tema.of(it) },
    typer = typer.map { Type.of(it) }
)

fun EnheterMedLovligeTemaer.mapToView() = this.enheter.map { enhet -> enhet.mapToView() }

fun EnhetMedLovligeTemaer.mapToView() =
    EnhetView(
        id = enhetId,
        navn = navn,
        lovligeTemaer = temaer.map { tema -> tema.id }
    )
