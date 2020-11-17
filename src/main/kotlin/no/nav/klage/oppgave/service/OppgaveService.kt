package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.view.OppgaverRespons
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import no.nav.klage.oppgave.domain.view.Oppgave as OppgaveView


@Service
class OppgaveService(
    val oppgaveClient: OppgaveClient,
    val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    val saksbehandlerRepository: SaksbehandlerRepository,
    val oppgaveMapper: OppgaveMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun searchOppgaver(navIdent: String, oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }

        oppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetIdent)

        val oppgaveResponse = oppgaveClient.getOneSearchPage(oppgaverSearchCriteria)
        return OppgaverRespons(
            antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
            oppgaver = oppgaveMapper.mapOppgaverToView(
                oppgaveResponse.oppgaver,
                oppgaverSearchCriteria.isProjectionUtvidet()
            )
        )
    }

    private fun OppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetIdent: String) {
        val tilgangerForSaksbehandler = saksbehandlerRepository.getTilgangerForSaksbehandler(innloggetIdent)
        if (tilgangerForSaksbehandler.enheter.size > 1) {
            logger.warn("Saksbehandler ({}) had more than one enhet. Only using the first.", innloggetIdent)
        }
        this.enhetsnr = tilgangerForSaksbehandler.enheter.first().enhetId
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
            tilordnetRessurs = saksbehandlerIdent;
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