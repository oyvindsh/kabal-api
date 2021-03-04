package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.api.view.OppgaverQueryParams
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.exceptions.NotOwnEnhetException
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class OppgaverQueryParamsMapper(private val saksbehandlerRepository: SaksbehandlerRepository) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun toSearchCriteria(navIdent: String, queryParams: OppgaverQueryParams) = KlagebehandlingerSearchCriteria(
        typer = queryParams.typer.map { Sakstype.fromNavn(it) },
        temaer = queryParams.temaer.map { Tema.fromNavn(it) },
        hjemler = queryParams.hjemler,
        order = if (queryParams.rekkefoelge == OppgaverQueryParams.Rekkefoelge.SYNKENDE) {
            KlagebehandlingerSearchCriteria.Order.DESC
        } else {
            KlagebehandlingerSearchCriteria.Order.ASC
        },
        offset = queryParams.start,
        limit = queryParams.antall,
        erTildeltSaksbehandler = queryParams.erTildeltSaksbehandler,
        saksbehandler = queryParams.tildeltSaksbehandler,
        projection = queryParams.projeksjon?.let { KlagebehandlingerSearchCriteria.Projection.valueOf(it.name) },
        enhetsnr = validateAndGetEnhetId(navIdent, queryParams.enhetId),
        sortField = if (queryParams.sortering == OppgaverQueryParams.Sortering.MOTTATT) {
            KlagebehandlingerSearchCriteria.SortField.MOTTATT
        } else {
            KlagebehandlingerSearchCriteria.SortField.FRIST
        }
    )

    fun toFristUtgaattIkkeTildeltSearchCriteria(navIdent: String, oppgaverQueryParams: OppgaverQueryParams) =
        KlagebehandlingerSearchCriteria(
            typer = oppgaverQueryParams.typer.map { Sakstype.fromNavn(it) },
            temaer = oppgaverQueryParams.temaer.map { Tema.fromNavn(it) },
            hjemler = oppgaverQueryParams.hjemler,
            offset = 0,
            limit = 1,
            erTildeltSaksbehandler = false,
            enhetsnr = validateAndGetEnhetId(navIdent, oppgaverQueryParams.enhetId),
            fristFom = LocalDate.now().minusYears(15),
            fristTom = LocalDate.now().minusDays(1),
        )

    private fun validateAndGetEnhetId(navIdent: String, enhetId: String): String {
        val tilgangerForSaksbehandler = saksbehandlerRepository.getTilgangerForSaksbehandler(navIdent)

        if (tilgangerForSaksbehandler.enheter.none { e -> e.enhetId == enhetId }) {
            throw NotOwnEnhetException("$navIdent is not part of enhet $enhetId")
        }
        return enhetId
    }

}


