package no.nav.klage.oppgave.api.view

import no.nav.klage.oppgave.domain.kodeverk.MedunderskriverFlyt

data class MedunderskriverInfoView (
    val medunderskriver: SaksbehandlerRefView?,
    val medunderskriverFlyt: MedunderskriverFlyt
)
