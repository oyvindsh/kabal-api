package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Oppgavereferanse
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class KlagebehandlingService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val hjemmelService: HjemmelService,
    private val kodeverkService: KodeverkService
) {

    fun fetchKlagesakForOppgaveKopi(oppgaveId: Long): Klagebehandling? =
        klagebehandlingRepository.findByOppgavereferanserOppgaveId(oppgaveId)

    fun connectOppgaveKopiToKlagebehandling(oppgaveKopi: OppgaveKopi): UUID {
        val klagesak = fetchKlagesakForOppgaveKopi(oppgaveKopi.id)
        if (klagesak != null) {
            return klagesak.id
        }

        val createdKlagebehandling = klagebehandlingRepository.save(Klagebehandling(
            foedselsnummer = oppgaveKopi.ident?.verdi ?: throw java.lang.RuntimeException("Missing ident on oppgave ${oppgaveKopi.id}"),
            frist = oppgaveKopi.fristFerdigstillelse ?: calculateFrist(),
            sakstype = kodeverkService.getSakstypeFromBehandlingstema(oppgaveKopi.behandlingstype),
            hjemler = hjemmelService.getHjemmelFromOppgaveKopi(oppgaveKopi),
            oppgavereferanser = listOf(
                Oppgavereferanse(
                    oppgaveId = oppgaveKopi.id
                )
            )
        ))

        return createdKlagebehandling.id
    }

    // TODO Implement a proper algorithm
    private fun calculateFrist() = LocalDate.now().plusDays(100)
}
