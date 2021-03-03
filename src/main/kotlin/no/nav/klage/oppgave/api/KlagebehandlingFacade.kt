package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.KvalitetsvurderingInput
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class KlagebehandlingFacade(
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val klagebehandlingService: KlagebehandlingService,
    private val elasticsearchRepository: ElasticsearchRepository,
    private val oppgaveMapper: OppgaveMapper,
    private val oppgaveService: OppgaveService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun getKlagebehandling(klagebehandlingId: UUID): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.getKlagebehandling(
                klagebehandlingId
            )
        )
    }

    fun searchKlagebehandlinger(searchCriteria: KlagebehandlingerSearchCriteria): KlagebehandlingerListRespons {
        val esResponse = elasticsearchRepository.findByCriteria(searchCriteria)
        return KlagebehandlingerListRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            klagebehandlinger = klagebehandlingMapper.mapEsKlagebehandlingerToListView(
                esResponse.searchHits.map { it.content },
                searchCriteria.isProjectionUtvidet()
            )
        )
    }

    fun searchOppgaver(klagebehandlingerSearchCriteria: KlagebehandlingerSearchCriteria): OppgaverRespons {
        val esResponse = elasticsearchRepository.findByCriteria(klagebehandlingerSearchCriteria)
        return OppgaverRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            oppgaver = oppgaveMapper.mapEsKlagebehandlingerToView(
                esResponse.searchHits.map { it.content },
                klagebehandlingerSearchCriteria.isProjectionUtvidet()
            )
        )
    }

    fun countOppgaver(klagebehandlingerSearchCriteria: KlagebehandlingerSearchCriteria): AntallUtgaatteFristerResponse {
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchRepository.countByCriteria(klagebehandlingerSearchCriteria)
        )
    }

    fun assignKlagebehandling(klagebehandlingId: UUID, saksbehandlerIdent: String?) {
        klagebehandlingService.assignKlagebehandling(klagebehandlingId, saksbehandlerIdent)
            .also { indexKlagebehandling(it) }
        val oppgaveIderForKlagebehandling = klagebehandlingService.getOppgaveIderForKlagebehandling(klagebehandlingId)

        oppgaveIderForKlagebehandling.forEach {
            try {
                oppgaveService.assignOppgave(it, saksbehandlerIdent)
            } catch (e: Exception) {
                logger.error(
                    "Unable to assign oppgave {} to saksbehandler {}, klagebehandling {} is not in sync",
                    it,
                    saksbehandlerIdent,
                    klagebehandlingId,
                    e
                )
            }
        }
    }

    fun getKvalitetsvurdering(klagebehandlingId: UUID): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.getKvalitetsvurdering(klagebehandlingId)
        )
    }

    fun updateKvalitetsvurdering(
        klagebehandlingId: UUID,
        kvalitetsvurderingInput: KvalitetsvurderingInput
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.updateKvalitetsvurdering(klagebehandlingId, kvalitetsvurderingInput)
                .also { indexKlagebehandling(it) }.kvalitetsvurdering
        )
    }

    fun indexKlagebehandling(klagebehandling: Klagebehandling) {
        try {
            elasticsearchRepository.save(
                klagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling)
            )
        } catch (e: Exception) {
            if (e.message?.contains("version_conflict_engine_exception") == true) {
                logger.info("Later version already indexed, ignoring this..")
            } else {
                logger.error("Unable to index klagebehandling ${klagebehandling.id}, see securelogs for details")
                securelogger.error("Unable to index klagebehandling ${klagebehandling.id}", e)
            }
        }
    }
}