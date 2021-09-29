package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.axsys.AxsysGateway
import org.springframework.stereotype.Service

@Service
class EnhetRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val axsysGateway: AxsysGateway
) {

    fun getAnsatteIEnhet(enhetId: String): List<String> {
        return axsysGateway.getSaksbehandlereIEnhet(enhetId).map { it.navIdent }
    }

    fun getLedereIEnhet(enhetId: String): List<String> {
        return axsysGateway.getSaksbehandlereIEnhet(enhetId)
            .filter { saksbehandlerRepository.erLeder(it.navIdent) }
            .map { it.navIdent }
    }

    fun getFagansvarligeIEnhet(enhetId: String): List<String> {
        return axsysGateway.getSaksbehandlereIEnhet(enhetId)
            .filter { saksbehandlerRepository.erFagansvarlig(it.navIdent) }
            .map { it.navIdent }
    }


}