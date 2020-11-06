package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.OppgaveClient
import no.nav.klage.oppgave.clients.PdlClient
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.gosys.*
import no.nav.klage.oppgave.domain.gosys.Gruppe.FOLKEREGISTERIDENT
import no.nav.klage.oppgave.domain.pdl.Navn
import no.nav.klage.oppgave.domain.view.*
import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service


@Service
class OppgaveService(
    val oppgaveClient: OppgaveClient,
    val pdlClient: PdlClient,
    val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    val saksbehandlerRepository: SaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun searchTildelteOppgaver(
        navIdent: String,
        oppgaverSearchCriteria: OppgaverSearchCriteria
    ): TildelteOppgaverRespons {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException("logged in user does not match sent in user. Logged in: $innloggetIdent, sent in: $navIdent")
        }

        oppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetIdent)

        val oppgaveResponse = oppgaveClient.getOneSearchPage(oppgaverSearchCriteria)
        return TildelteOppgaverRespons(
            antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
            oppgaver = oppgaveResponse.toTildelteOppgaverView()
        )
    }

    fun searchIkkeTildelteOppgaver(oppgaverSearchCriteria: OppgaverSearchCriteria): IkkeTildelteOppgaverRespons {
        oppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetSaksbehandlerRepository.getInnloggetIdent())

        val oppgaveResponse = oppgaveClient.getOneSearchPage(oppgaverSearchCriteria)
        return IkkeTildelteOppgaverRespons(
            antallTreffTotalt = oppgaveResponse.antallTreffTotalt,
            oppgaver = oppgaveResponse.toIkkeTildelteOppgaverView()
        )
    }

    private fun OppgaverSearchCriteria.enrichWithEnhetsnrForLoggedInUser(innloggetIdent: String) {
        val tilgangerForSaksbehandler = saksbehandlerRepository.getTilgangerForSaksbehandler(innloggetIdent)
        this.enhetsnr = tilgangerForSaksbehandler.enheter.first().enhetId
    }

    private fun OppgaveResponse.toTildelteOppgaverView(): List<TildeltOppgave> {
        val brukere = getBrukere(getFnr(this.oppgaver))

        return oppgaver.map { oppgave ->
            TildeltOppgave(
                id = oppgave.id.toString(),
                bruker = brukere[oppgave.getFnrForBruker()] ?: TildeltOppgave.Bruker("Mangler fnr", "Mangler fnr"),
                type = oppgave.toType(),
                ytelse = oppgave.toYtelse(),
                hjemmel = oppgave.metadata.toHjemmel(),
                frist = oppgave.fristFerdigstillelse,
                versjon = oppgave.versjon
            )
        }
    }

    private fun OppgaveResponse.toIkkeTildelteOppgaverView(): List<IkkeTildeltOppgave> {
        return oppgaver.map { oppgave ->
            IkkeTildeltOppgave(
                id = oppgave.id.toString(),
                type = oppgave.toType(),
                ytelse = oppgave.toYtelse(),
                hjemmel = oppgave.metadata.toHjemmel(),
                frist = oppgave.fristFerdigstillelse,
                versjon = oppgave.versjon
            )
        }
    }

    private fun getFnr(oppgaver: List<Oppgave>) =
        oppgaver.mapNotNull {
            it.getFnrForBruker()
        }

    private fun getBrukere(fnrList: List<String>): Map<String, TildeltOppgave.Bruker> {
        val people = pdlClient.getPersonInfo(fnrList).data?.hentPersonBolk
        return people?.map {
            val fnr = it.folkeregisteridentifikator.first().identifikasjonsnummer
            fnr to TildeltOppgave.Bruker(
                fnr = fnr,
                navn = it.navn.firstOrNull()?.toName() ?: "mangler"
            )
        }?.toMap() ?: emptyMap()
    }

    private fun Navn.toName() = "$fornavn $etternavn"

    private fun Map<String, String>?.toHjemmel() = this?.get(HJEMMEL) ?: "mangler"

    private fun Oppgave.toType(): String {
        return if (behandlingstema == null) {
            when (behandlingstype) {
                BEHANDLINGSTYPE_KLAGE -> TYPE_KLAGE
                BEHANDLINGSTYPE_ANKE -> TYPE_ANKE
                else -> "ukjent"
            }
        } else "mangler"
    }

    private fun Oppgave.toYtelse(): String = when (tema) {
        TEMA_SYK -> YTELSE_SYK
        TEMA_FOR -> YTELSE_FOR
        else -> tema
    }

    private fun Oppgave.getFnrForBruker() = identer?.find { i -> i.gruppe == FOLKEREGISTERIDENT }?.ident

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
//    fun assignOppgave(oppgaveId: Long, saksbehandlerIdent: String?, oppgaveVersjon: Int?): OppgaveView {
//        val oppgave = oppgaveRepository.getOppgave(oppgaveId).toEndreOppgave()
//        logger.info(
//            "Endrer tilordnetRessurs for oppgave {} fra {} til {}, versjon er {}",
//            oppgave.id,
//            oppgave.tilordnetRessurs,
//            saksbehandlerIdent,
//            oppgaveVersjon
//        )
//        oppgave.apply {
//            tilordnetRessurs = saksbehandlerIdent;
//            versjon = oppgaveVersjon
//        }
//
//        return updateAndReturn(oppgaveId, oppgave)
//    }
//
//    fun getOppgave(oppgaveId: Long): OppgaveView {
//        val oppgave = oppgaveRepository.getOppgave(oppgaveId)
//        val brukere = getBrukere(getFnr(listOf(oppgave)))
//        return toView(oppgave, brukere)
//    }
//
//    private fun updateAndReturn(
//        oppgaveId: Long,
//        oppgave: EndreOppgave
//    ): OppgaveView {
//        val endretOppgave = oppgaveRepository.updateOppgave(oppgaveId, oppgave)
//        val brukere = getBrukere(getFnr(listOf(endretOppgave)))
//        return toView(endretOppgave, brukere)
//    }

}