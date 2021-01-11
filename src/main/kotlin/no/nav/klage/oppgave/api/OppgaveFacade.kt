package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.internal.OppgaveKopiAPIModel
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.api.view.Oppgave
import no.nav.klage.oppgave.api.view.OppgaverRespons
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.service.OppgaveKopiService
import no.nav.klage.oppgave.service.OppgaveService
import org.springframework.stereotype.Service

@Service
class OppgaveFacade(
    private val oppgaveService: OppgaveService,
    private val oppgaveMapper: OppgaveMapper,
    private val oppgaveKopiService: OppgaveKopiService
) {

    fun searchOppgaver(oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        val oppgaveResponse = oppgaveService.searchOppgaver(oppgaverSearchCriteria)
        return OppgaverRespons(
            antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
            oppgaver = oppgaveMapper.mapOppgaverToView(
                oppgaveResponse.oppgaver,
                oppgaverSearchCriteria.isProjectionUtvidet()
            )
        )
    }

    fun assignOppgave(oppgaveId: Long, saksbehandlerIdent: String?, oppgaveVersjon: Int?) {
        oppgaveService.assignOppgave(oppgaveId, saksbehandlerIdent, oppgaveVersjon)
    }

    fun getOppgave(oppgaveId: Long): Oppgave {
        val oppgaveBackend = oppgaveService.getOppgave(oppgaveId)
        return oppgaveMapper.mapOppgaveToView(oppgaveBackend, true)
    }

    fun saveOppgaveKopi(oppgave: OppgaveKopiAPIModel) {
        oppgaveKopiService.saveOppgaveKopi(oppgaveMapper.mapOppgaveKopiAPIModelToOppgaveKopi(oppgave))
    }
}