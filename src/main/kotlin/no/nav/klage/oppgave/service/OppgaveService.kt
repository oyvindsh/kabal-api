package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.view.OppgaverRespons
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import no.nav.klage.oppgave.domain.view.Oppgave as OppgaveView

@Service
class OppgaveService(
    val oppgaveClient: OppgaveClient,
    val oppgaveMapper: OppgaveMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun searchOppgaver(oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        val oppgaveResponse = oppgaveClient.getOneSearchPage(oppgaverSearchCriteria)
        return OppgaverRespons(
            antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
            oppgaver = oppgaveMapper.mapOppgaverToView(
                oppgaveResponse.oppgaver,
                oppgaverSearchCriteria.isProjectionUtvidet()
            )
        )
    }

    fun assignOppgave(oppgaveId: Long, saksbehandlerIdent: String?, oppgaveVersjon: Int?) {
        val endreOppgave = oppgaveClient.getOppgave(oppgaveId).toEndreOppgave()
        logger.info(
            "Endrer tilordnetRessurs for oppgave {} fra {} til {}, versjon er {}",
            endreOppgave.id,
            endreOppgave.tilordnetRessurs,
            saksbehandlerIdent,
            oppgaveVersjon
        )
        endreOppgave.apply {
            tilordnetRessurs = saksbehandlerIdent
            versjon = oppgaveVersjon
        }

        oppgaveClient.putOppgave(oppgaveId, endreOppgave)
    }

    fun getOppgave(oppgaveId: Long): OppgaveView {
        val oppgaveBackend = oppgaveClient.getOppgave(oppgaveId)
        return oppgaveMapper.mapOppgaveToView(oppgaveBackend, true)
    }


//    fun setHjemmel(oppgaveId: Long, hjemmel: String, oppgaveVersjon: Int?): OppgaveView {
//        val oppgave = oppgaveRepository.getOppgave(oppgaveId).toEndreOppgave()
//        oppgave.apply {
//            setHjemmel(hjemmel)
//            versjon = oppgaveVersjon
//        }
//
//        return updateAndReturn(oppgaveId, oppgave)
//    }
//
//    private fun EndreOppgave.setHjemmel(hjemmel: String) {
//        if (metadata == null) {
//            metadata = mutableMapOf()
//        }
//        logger.info("Endrer hjemmel for oppgave {} fra {} til {}", id, metadata?.get(HJEMMEL), hjemmel)
//        metadata!![HJEMMEL] = hjemmel
//    }
//

}