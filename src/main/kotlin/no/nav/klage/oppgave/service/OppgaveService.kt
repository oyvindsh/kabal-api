package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.AxsysClient
import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.OppgaveResponse
import no.nav.klage.oppgave.domain.view.OppgaveView
import no.nav.klage.oppgave.domain.view.OppgaveView.Bruker
import no.nav.security.token.support.client.core.oauth2.OAuth2AccessTokenService
import no.nav.security.token.support.client.spring.ClientConfigurationProperties
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OppgaveService(
    val clientConfigurationProperties: ClientConfigurationProperties,
    val oAuth2AccessTokenService: OAuth2AccessTokenService,
    val axsysClient: AxsysClient,
    val microsoftGraphClient: MicrosoftGraphClient,
    val oppgaveClient: OppgaveClient
) {

    fun getOppgaver(): List<OppgaveView> {
        return oppgaveClient.getOppgaver().toView()
    }

    fun getTilgangerForSaksbehandler() =
        axsysClient.getTilgangerForSaksbehandler(microsoftGraphClient.getNavIdent(getTokenWithGraphScope()))

    private fun getTokenWithGraphScope(): String {
        val clientProperties = clientConfigurationProperties.registration["onbehalfof"]
        val response = oAuth2AccessTokenService.getAccessToken(clientProperties)
        return response.accessToken
    }

    private fun OppgaveResponse.toView(): List<OppgaveView> {
        return oppgaver.map {
            OppgaveView(
                id = it.id,
                bruker = Bruker(
                    fnr = "aktørId pr nå: " + it.aktoerId,
                    navn = "TODO navn"
                ),
                type = "TODO Klage/Anke",
                ytelse = it.tema,
                hjemmel = listOf("TODO hjemmel"),
                frist = LocalDate.parse(it.fristFerdigstillelse),
                saksbehandler = "TODO saksbehandler"
            )
        }
    }
}

