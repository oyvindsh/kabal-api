package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.internal.OppgaveKopiAPIModel
import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.service.OppgaveKopiService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class OppgaveFacade(
    private val oppgaveMapper: OppgaveMapper,
    private val klagebehandlingMapper: KlagebehandlingMapper
    private val oppgaveKopiService: OppgaveKopiService,
    private val elasticsearchRepository: ElasticsearchRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun saveOppgaveKopi(oppgave: OppgaveKopiAPIModel) {
        val klagebehandlingerOgMottak =
            oppgaveKopiService.saveOppgaveKopi(oppgaveMapper.mapOppgaveKopiAPIModelToOppgaveKopi(oppgave))

        try {
            klagebehandlingerOgMottak.forEach {
                elasticsearchRepository.save(
                    klagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(it)
                )
            }
        } catch (e: Exception) {
            if (e.message?.contains("version_conflict_engine_exception") == true) {
                logger.info("Later version already indexed, ignoring this..")
            } else {
                logger.error("Unable to index OppgaveKopi, see securelogs for details")
                securelogger.error("Unable to index OppgaveKopi", e)
            }
        }

    }
}


