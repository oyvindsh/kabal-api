package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.internal.OppgaveKopiAPIModel
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.service.OppgaveKopiService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class OppgaveFacade(
    private val oppgaveMapper: OppgaveMapper,
    private val klagebehandlingFacade: KlagebehandlingFacade,
    private val oppgaveKopiService: OppgaveKopiService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun saveOppgaveKopi(oppgave: OppgaveKopiAPIModel) {
        val klagebehandlingerOgMottak =
            oppgaveKopiService.saveOppgaveKopi(oppgaveMapper.mapOppgaveKopiAPIModelToOppgaveKopi(oppgave))

        klagebehandlingerOgMottak.forEach {
            klagebehandlingFacade.indexKlagebehandling(it.first)
        }

    }
}


