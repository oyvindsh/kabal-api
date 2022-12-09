package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.kabalinnstillinger.KabalInnstillingerClient
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Medunderskrivere
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.MedunderskrivereInput
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.SaksbehandlerSearchInput
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Saksbehandlere
import org.springframework.stereotype.Service
import java.util.*

@Service
class KabalInnstillingerService(
    private val behandlingService: BehandlingService,
    private val kabalInnstillingerClient: KabalInnstillingerClient,
) {
    fun getPotentialSaksbehandlere(behandlingId: UUID): Saksbehandlere {
        val behandling = behandlingService.getBehandling(behandlingId)
        return kabalInnstillingerClient.searchSaksbehandlere(
            SaksbehandlerSearchInput(
                ytelseId = behandling.ytelse.id,
                fnr = behandling.sakenGjelder.partId.value,
            )
        )
    }

    fun getPotentialMedunderskrivere(behandlingId: UUID): Medunderskrivere {
        val behandling = behandlingService.getBehandling(behandlingId)
        if (behandling.tildeling == null) {
            return Medunderskrivere(medunderskrivere = emptyList())
        }
        return kabalInnstillingerClient.searchMedunderskrivere(
            MedunderskrivereInput(
                ytelseId = behandling.ytelse.id,
                fnr = behandling.sakenGjelder.partId.value,
                enhet = behandling.tildeling!!.enhet!!,
                navIdent = behandling.tildeling!!.saksbehandlerident!!
            )
        )
    }

}