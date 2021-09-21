package no.nav.klage.oppgave.gateway

import no.nav.klage.oppgave.clients.azure.MicrosoftGraphClient
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerRolle
import org.springframework.stereotype.Service

@Service
class AzureGateway(private val microsoftGraphClient: MicrosoftGraphClient) {

    fun getRolleIder(ident: String): List<String> {
        return getRollerForSaksbehandlerMedIdent(ident).map { it.id }
    }

    fun getGroupMembersNavIdents(groupid: String): List<String> {
        return microsoftGraphClient.getGroupMembersNavIdents(groupid)
    }

    fun getAllDisplayNames(idents: List<List<String>>): Map<String, String> {
        return microsoftGraphClient.getAllDisplayNames(idents)
    }

    fun getPersonligDataOmSaksbehandlerMedIdent(navIdent: String): SaksbehandlerPersonligInfo {
        val data = microsoftGraphClient.getSaksbehandler(navIdent)
        return SaksbehandlerPersonligInfo(
            data.onPremisesSamAccountName,
            data.id,
            data.givenName,
            data.surname,
            data.displayName,
            data.mail
        )
    }

    fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo {
        val data = microsoftGraphClient.getInnloggetSaksbehandler()
        return SaksbehandlerPersonligInfo(
            data.onPremisesSamAccountName,
            data.id,
            data.givenName,
            data.surname,
            data.displayName,
            data.mail
        )
    }

    fun getRollerForSaksbehandlerMedIdent(navIdent: String): List<SaksbehandlerRolle> {
        return microsoftGraphClient.getSaksbehandlersGroups(navIdent)
            .map { SaksbehandlerRolle(it.id, it.displayName ?: it.id) }
    }

    fun getRollerForInnloggetSaksbehandler(): List<SaksbehandlerRolle> {
        return microsoftGraphClient.getInnloggetSaksbehandlersGroups()
            .map { SaksbehandlerRolle(it.id, it.displayName ?: it.id) }
    }


}