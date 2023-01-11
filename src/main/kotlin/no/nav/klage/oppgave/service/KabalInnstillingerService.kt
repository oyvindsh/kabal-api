package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.clients.kabalinnstillinger.KabalInnstillingerClient
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Medunderskrivere
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.MedunderskrivereInput
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.SaksbehandlerSearchInput
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Saksbehandlere
import no.nav.klage.oppgave.domain.Behandling
import org.springframework.stereotype.Service

@Service
class KabalInnstillingerService(
    private val kabalInnstillingerClient: KabalInnstillingerClient,
) {
    fun getPotentialSaksbehandlere(behandling: Behandling): Saksbehandlere {
        return kabalInnstillingerClient.searchSaksbehandlere(
            SaksbehandlerSearchInput(
                ytelseId = behandling.ytelse.id,
                fnr = behandling.sakenGjelder.partId.value,
            )
        )
    }

    fun getPotentialMedunderskrivere(behandling: Behandling): Medunderskrivere {
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

    //TODO: Bør vi ha et cache her? Kan være et problem om leder gir nye tilganger, kanskje et kortere cache?
    fun getTildelteYtelserForSaksbehandler(navIdent: String): List<Ytelse> {
        return kabalInnstillingerClient.getSaksbehandlersTildelteYtelser(navIdent).ytelseIdList.map {
            Ytelse.of(it)
        }
    }

}