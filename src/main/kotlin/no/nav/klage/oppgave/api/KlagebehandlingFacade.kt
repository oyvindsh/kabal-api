package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.mapper.OppgaveMapper
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.api.view.OppgaverRespons
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.klage.KvalitetsvurderingInput
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
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

    fun searchOppgaver(oppgaverSearchCriteria: OppgaverSearchCriteria): OppgaverRespons {
        val esResponse = elasticsearchRepository.findByCriteria(oppgaverSearchCriteria)
        return OppgaverRespons(
            antallTreffTotalt = esResponse.totalHits.toInt(),
            oppgaver = oppgaveMapper.mapEsKlagebehandlingerToView(
                esResponse.searchHits.map { it.content },
                oppgaverSearchCriteria.isProjectionUtvidet()
            )
        )
    }

    fun assignOppgave(klagebehandlingId: UUID, saksbehandlerIdent: String?) {
        klagebehandlingService.assignOppgave(klagebehandlingId, saksbehandlerIdent)
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
            klagebehandlingService.getKlagebehandling(klagebehandlingId)
        )
    }

    fun updateKvalitetsvurdering(
        klagebehandlingId: UUID,
        kvalitetsvurderingInput: KvalitetsvurderingInput
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.updateKvalitetsvurdering(klagebehandlingId, kvalitetsvurderingInput)
        )
    }
}