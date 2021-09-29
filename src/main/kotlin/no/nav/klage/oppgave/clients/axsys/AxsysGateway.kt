package no.nav.klage.oppgave.clients.axsys

import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerIdent
import no.nav.klage.oppgave.gateway.IAxsysGateway
import org.springframework.stereotype.Service

@Service
class AxsysGateway(
    private val axsysClient: AxsysClient,
    private val tilgangerMapper: TilgangerMapper
) : IAxsysGateway {

    override fun getEnheterMedTemaerForSaksbehandler(ident: String): EnheterMedLovligeTemaer =
        tilgangerMapper.mapTilgangerToEnheterMedLovligeTemaer(axsysClient.getTilgangerForSaksbehandler(ident))

    override fun getSaksbehandlereIEnhet(enhetId: String): List<SaksbehandlerIdent> {
        return axsysClient.getSaksbehandlereIEnhet(enhetId).map { SaksbehandlerIdent(it.appIdent) }
    }
}