package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.gateway.AzureGateway
import org.springframework.stereotype.Service

@Service
class EnhetRepository(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val azureGateway: AzureGateway
) {

    fun getAnsatteIEnhet(enhetId: String): List<String> {
        return azureGateway.getAnsatteIEnhet(enhetId).map { it.navIdent }
    }

    fun getLedereIEnhet(enhetId: String): List<String> {
        return azureGateway.getAnsatteIEnhet(enhetId)
            .filter { saksbehandlerRepository.erLeder(it.navIdent) }
            .map { it.navIdent }
    }

    fun getFagansvarligeIEnhet(enhetId: String): List<String> {
        return azureGateway.getAnsatteIEnhet(enhetId)
            .filter { saksbehandlerRepository.erFagansvarlig(it.navIdent) }
            .map { it.navIdent }
    }


}