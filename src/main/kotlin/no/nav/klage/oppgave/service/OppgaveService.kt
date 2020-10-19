package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.AxsysClient
import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.gosys.BEHANDLINGSTYPE_KLAGE
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.Oppgave.Gruppe.FOLKEREGISTERIDENT
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.HJEMMEL
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.domain.view.OppgaveView.Bruker
import no.nav.klage.oppgave.domain.view.TYPE_FEILUTBETALING
import no.nav.klage.oppgave.domain.view.TYPE_KLAGE
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service

@Service
class OppgaveService(
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
    val axsysClient: AxsysClient,
    val microsoftGraphClient: MicrosoftGraphClient,
    val oppgaveClient: OppgaveClient,
    val pdlClient: PdlClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getOppgaver(): List<OppgaveView> {
        return oppgaveClient.getOppgaver().toView().also { logger.info("Returnerer {} oppgaver", it.size) }
    }

    fun getTilgangerForSaksbehandler() =
        axsysClient.getTilgangerForSaksbehandler(microsoftGraphClient.getNavIdent(getTokenWithGraphScope()))

    private fun getTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    private fun OppgaveResponse.toView(): List<OppgaveView> {
        val brukere = getBrukere(getFnr(oppgaver))
        val saksbehandlere = getSaksbehandlere(getSaksbehandlerIdenter(oppgaver))
        return oppgaver.map {
            OppgaveView(
                id = it.id,
                bruker = brukere[it.getFnrForBruker()] ?: Bruker("Mangler fnr", "Mangler fnr"),
                type = it.toType(),
                ytelse = it.tema,
                hjemmel = it.metadata.toHjemmel(),
                frist = it.fristFerdigstillelse,
                saksbehandler = saksbehandlere[it.tilordnetRessurs]
            )
        }
    }

    private fun getSaksbehandlere(identer: Set<String>): Map<String, OppgaveView.Saksbehandler> {
        logger.debug("Getting names for saksbehandlere")
        val namesForSaksbehandlere = microsoftGraphClient.getNamesForSaksbehandlere(identer, getTokenWithGraphScope())
        return namesForSaksbehandlere.map {
            it.key to OppgaveView.Saksbehandler(
                ident = it.key,
                navn = it.value
            )
        }.toMap().also {
            logger.debug("Names returned: {}", it)
        }
    }

    private fun getFnr(oppgaver: List<Oppgave>) =
        oppgaver.mapNotNull {
            it.getFnrForBruker()
        }

    private fun getSaksbehandlerIdenter(oppgaver: List<Oppgave>) =
        oppgaver.mapNotNull {
            it.tilordnetRessurs
        }.toSet()

    private fun getBrukere(fnrList: List<String>): Map<String, Bruker> {
        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk
        return people?.map {
            val fnr = it.folkeregisteridentifikator.first().identifikasjonsnummer
            fnr to Bruker(
                fnr = fnr,
                navn = it.navn.firstOrNull()?.toName() ?: "mangler"
            )
        }?.toMap() ?: emptyMap()
    }

    private fun Navn.toName() = "$fornavn $etternavn"

    private fun Map<String, String>?.toHjemmel() = listOf(this?.get(HJEMMEL) ?: "mangler")

    private fun Oppgave.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_FEILUTBETALING -> TYPE_FEILUTBETALING
                else -> "mangler"
            }
        } else "mangler"
    }

    private fun Oppgave.getFnrForBruker() = identer?.find { i -> i.gruppe == FOLKEREGISTERIDENT }?.ident
}