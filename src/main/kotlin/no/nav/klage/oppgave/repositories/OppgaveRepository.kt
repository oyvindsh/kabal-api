package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.OppgaverQueryParams
import no.nav.klage.oppgave.domain.gosys.EndreOppgave
import no.nav.klage.oppgave.domain.gosys.Oppgave
import no.nav.klage.oppgave.domain.gosys.OppgaveResponse
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class OppgaveRepository(
    private val oppgaveClient: OppgaveClient,
    private val microsoftGraphClient: MicrosoftGraphClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun searchOppgaver(oppgaverSearchCriteria: OppgaverQueryParams, saksbehandlerIdent: String?): OppgaveResponse {
        logger.debug("Searching for oppgaver")

        return oppgaveClient.getOneSearchPage(oppgaverSearchCriteria, saksbehandlerIdent).also {
            logger.debug("Retrieved {} of {} oppgaver in search", it.oppgaver.size, it.antallTreffTotalt)
        }
    }

    fun updateOppgave(oppgaveId: Long, oppgave: EndreOppgave): Oppgave =
        oppgaveClient.putOppgave(oppgaveId, oppgave)

    fun getOppgave(oppgaveId: Long): Oppgave =
        oppgaveClient.getOppgave(oppgaveId)
}