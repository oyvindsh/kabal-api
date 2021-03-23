package no.nav.klage.oppgave.api.facade

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.AntallUtgaatteFristerResponse
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.api.view.KlagebehandlingerListRespons
import no.nav.klage.oppgave.api.view.KvalitetsvurderingView
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.service.OppgaveService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.time.LocalDate
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

    fun assignKlagebehandling(
        klagebehandlingId: UUID,
        tildeltSaksbehandlerident: String?,
        utfoerendeSaksbehandlerident: String
    ) {
        klagebehandlingService.assignKlagebehandling(
            klagebehandlingId,
            tildeltSaksbehandlerident,
            utfoerendeSaksbehandlerident
        )
        val oppgaveIderForKlagebehandling = klagebehandlingService.getOppgaveIderForKlagebehandling(klagebehandlingId)

        oppgaveIderForKlagebehandling.forEach {
            try {
                oppgaveService.assignOppgave(it, tildeltSaksbehandlerident)
            } catch (e: Exception) {
                logger.error(
                    "Unable to assign klagebehandling {} to saksbehandler {}, klagebehandling {} is not in sync",
                    it,
                    tildeltSaksbehandlerident,
                    klagebehandlingId,
                    e
                )
            }
        }
    }

    fun setSakstype(klagebehandlingId: UUID, sakstype: Sakstype, saksbehandlerIdent: String): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setSakstype(klagebehandlingId, sakstype, saksbehandlerIdent)
        )
    }

    fun setTema(klagebehandlingId: UUID, tema: Tema, saksbehandlerIdent: String): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setTema(klagebehandlingId, tema, saksbehandlerIdent)
        )
    }

    fun setInnsendt(klagebehandlingId: UUID, innsendt: LocalDate, saksbehandlerIdent: String): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setInnsendt(klagebehandlingId, innsendt, saksbehandlerIdent)
        )
    }

    fun setMottattFoersteinstans(
        klagebehandlingId: UUID,
        mottattFoersteinstans: LocalDate,
        saksbehandlerIdent: String
    ): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setMottattFoersteinstans(
                klagebehandlingId,
                mottattFoersteinstans,
                saksbehandlerIdent
            )
        )
    }

    fun setMottattKlageinstans(
        klagebehandlingId: UUID,
        mottattKlageinstans: LocalDate,
        saksbehandlerIdent: String
    ): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setMottattKlageinstans(
                klagebehandlingId,
                mottattKlageinstans,
                saksbehandlerIdent
            )
        )
    }

    fun setFrist(
        klagebehandlingId: UUID,
        frist: LocalDate,
        saksbehandlerIdent: String
    ): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setFrist(
                klagebehandlingId,
                frist,
                saksbehandlerIdent
            )
        )
    }

    fun setAvsenderSaksbehandleridentFoersteinstans(
        klagebehandlingId: UUID,
        avsenderSaksbehandlerident: String,
        saksbehandlerIdent: String
    ): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setAvsenderSaksbehandleridentFoersteinstans(
                klagebehandlingId,
                avsenderSaksbehandlerident,
                saksbehandlerIdent
            )
        )
    }

    fun setAvsenderEnhetFoersteinstans(
        klagebehandlingId: UUID,
        avsenderEnhet: String,
        saksbehandlerIdent: String
    ): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.setAvsenderEnhetFoersteinstans(
                klagebehandlingId,
                avsenderEnhet,
                saksbehandlerIdent
            )
        )
    }

    fun getKvalitetsvurdering(klagebehandlingId: UUID): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.getKvalitetsvurdering(klagebehandlingId)
        )
    }

    fun setKvalitetsvurderingGrunn(
        klagebehandlingId: UUID,
        grunn: Grunn?,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingGrunn(
                klagebehandlingId,
                grunn,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }

    fun setKvalitetsvurderingEoes(
        klagebehandlingId: UUID,
        eoes: Eoes?,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingEoes(
                klagebehandlingId,
                eoes,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }

    fun setKvalitetsvurderingRaadfoertMedLege(
        klagebehandlingId: UUID,
        raadfoertMedLege: RaadfoertMedLege?,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingRaadfoertMedLege(
                klagebehandlingId,
                raadfoertMedLege,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }

    fun setKvalitetsvurderingInternVurdering(
        klagebehandlingId: UUID,
        internVurdering: String?,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingInternVurdering(
                klagebehandlingId,
                internVurdering,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }

    fun setKvalitetsvurderingSendTilbakemelding(
        klagebehandlingId: UUID,
        sendTilbakemelding: Boolean?,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingSendTilbakemelding(
                klagebehandlingId,
                sendTilbakemelding,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }

    fun setKvalitetsvurderingTilbakemelding(
        klagebehandlingId: UUID,
        tilbakemelding: String?,
        saksbehandlerIdent: String
    ): KvalitetsvurderingView {
        return klagebehandlingMapper.mapKlagebehandlingToKvalitetsvurderingView(
            klagebehandlingService.setKvalitetsvurderingTilbakemelding(
                klagebehandlingId,
                tilbakemelding,
                saksbehandlerIdent
            ).kvalitetsvurdering
        )
    }


}