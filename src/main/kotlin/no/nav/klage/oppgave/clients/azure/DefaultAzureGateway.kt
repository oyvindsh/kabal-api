package no.nav.klage.oppgave.clients.azure

import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerPersonligInfo
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerRolle
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class DefaultAzureGateway(private val microsoftGraphClient: MicrosoftGraphClient) : AzureGateway {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    override fun getRolleIder(ident: String): List<String> {
        return getRollerForSaksbehandlerMedIdent(ident).map { it.id }
    }

    override fun getGroupMembersNavIdents(groupid: String): List<String> =
        try {
            microsoftGraphClient.getGroupMembersNavIdents(groupid)
        } catch (e: Exception) {
            logger.error("Failed to call getGroupMembersNavIdents", e)
            throw e
        }

    override fun getAllDisplayNames(idents: List<List<String>>): Map<String, String> =
        try {
            microsoftGraphClient.getAllDisplayNames(idents)
        } catch (e: Exception) {
            logger.error("Failed to call getAllDisplayNames", e)
            throw e
        }

    override fun getPersonligDataOmSaksbehandlerMedIdent(navIdent: String): SaksbehandlerPersonligInfo {
        val data = try {
            microsoftGraphClient.getSaksbehandler(navIdent)
        } catch (e: Exception) {
            logger.error("Failed to call getSaksbehandler", e)
            throw e
        }
        return SaksbehandlerPersonligInfo(
            data.onPremisesSamAccountName,
            data.id,
            data.givenName,
            data.surname,
            data.displayName,
            data.mail
        )
    }

    override fun getDataOmInnloggetSaksbehandler(): SaksbehandlerPersonligInfo {
        val data = try {
            microsoftGraphClient.getInnloggetSaksbehandler()
        } catch (e: Exception) {
            logger.error("Failed to call getInnloggetSaksbehandler", e)
            throw e
        }
        return SaksbehandlerPersonligInfo(
            data.onPremisesSamAccountName,
            data.id,
            data.givenName,
            data.surname,
            data.displayName,
            data.mail
        )
    }

    override fun getRollerForSaksbehandlerMedIdent(navIdent: String): List<SaksbehandlerRolle> =
        try {
            microsoftGraphClient.getSaksbehandlersGroups(navIdent)
                .map { SaksbehandlerRolle(it.id, it.displayName ?: it.mailNickname ?: it.id) }
        } catch (e: Exception) {
            logger.error("Failed to call getSaksbehandlersGroups", e)
            throw e
        }

    override fun getRollerForInnloggetSaksbehandler(): List<SaksbehandlerRolle> =
        try {
            microsoftGraphClient.getInnloggetSaksbehandlersGroups()
                .map { SaksbehandlerRolle(it.id, it.displayName ?: it.mailNickname ?: it.id) }
        } catch (e: Exception) {
            logger.error("Failed to call getInnloggetSaksbehandlersGroups", e)
            throw e
        }


}