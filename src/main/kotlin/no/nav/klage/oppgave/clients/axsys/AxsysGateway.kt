package no.nav.klage.oppgave.clients.axsys

import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerReferanse
import no.nav.klage.oppgave.gateway.IAxsysGateway
import org.springframework.stereotype.Service

@Service
class AxsysGateway(
    private val axsysClient: AxsysClient,
    private val tilgangerMapper: TilgangerMapper
) : IAxsysGateway {

    override fun getEnheterMedTemaerForSaksbehandler(ident: String): EnheterMedLovligeTemaer =
        tilgangerMapper.mapTilgangerToEnheterMedLovligeTemaer(axsysClient.getTilgangerForSaksbehandler(ident))

    override fun getSaksbehandlereIEnhet(enhetId: String): List<SaksbehandlerReferanse> {
        return axsysClient.getSaksbehandlereIEnhet(enhetId).map { SaksbehandlerReferanse(it.appIdent) }
    }
}