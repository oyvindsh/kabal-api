package no.nav.klage.oppgave.api.facade

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.AntallUtgaatteFristerResponse
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KlagebehandlingerListRespons
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.kodeverk.Grunn
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

    fun countOppgaver(klagebehandlingerSearchCriteria: KlagebehandlingerSearchCriteria): AntallUtgaatteFristerResponse {
        return AntallUtgaatteFristerResponse(
            antall = elasticsearchRepository.countByCriteria(klagebehandlingerSearchCriteria)
        )
    }

    fun assignKlagebehandling(klagebehandlingId: UUID, saksbehandlerIdent: String?) {
        klagebehandlingService.assignKlagebehandling(klagebehandlingId, saksbehandlerIdent)
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

    fun updateKvalitetsvurderingGrunn(
        klagebehandlingId: UUID,
        grunn: Grunn,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.updateKvalitetsvurderingGrunn(
                klagebehandlingId,
                grunn,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }


}