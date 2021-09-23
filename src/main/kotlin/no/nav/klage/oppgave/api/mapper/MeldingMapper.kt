package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.api.view.MeldingCreated
import no.nav.klage.oppgave.api.view.MeldingModified
import no.nav.klage.oppgave.api.view.MeldingView
import no.nav.klage.oppgave.domain.klage.Melding
import no.nav.klage.oppgave.service.SaksbehandlerService
import org.springframework.stereotype.Service

@Service
class MeldingMapper(
    private val saksbehandlerService: SaksbehandlerService
) {

    fun toCreatedView(melding: Melding): MeldingCreated {
        return MeldingCreated(
            created = melding.created
        )
    }

    fun toModifiedView(melding: Melding): MeldingModified {
        return MeldingModified(
            modified = melding.modified ?: throw RuntimeException("modified on melding not set")
        )
    }

    fun toMeldingerView(meldinger: List<Melding>): List<MeldingView> {
        val names = saksbehandlerService.getNamesForSaksbehandlere(
            meldinger.map { it.saksbehandlerident }.toSet()
        )

        return meldinger.map { melding ->
            MeldingView(
                id = melding.id,
                text = melding.text,
                author = MeldingView.Author(
                    saksbehandlerIdent = melding.saksbehandlerident,
                    name = names[melding.saksbehandlerident] ?: "ukjent navn",
                ),
                created = melding.created,
                modified = melding.modified
            )
        }
    }
}