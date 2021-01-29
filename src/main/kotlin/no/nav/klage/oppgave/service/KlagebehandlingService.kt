package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Oppgavereferanse
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.SakstypeRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class KlagebehandlingService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val hjemmelService: HjemmelService,
    private val sakstypeRepository: SakstypeRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val klageinstansPrefix = "42"
    }

    fun fetchKlagesakForOppgaveKopi(oppgaveId: Long): Klagebehandling? =
        klagebehandlingRepository.findByOppgavereferanserOppgaveId(oppgaveId)

    fun connectOppgaveKopiToKlagebehandling(oppgaveKopi: OppgaveKopi) {
        val klagesak = fetchKlagesakForOppgaveKopi(oppgaveKopi.id)
        if (klagesak == null && oppgaveKopi.tildeltEnhetsnr.startsWith(klageinstansPrefix)) {
            requireNotNull(oppgaveKopi.ident)
            requireNotNull(oppgaveKopi.behandlingstype)

            val createdKlagebehandling = klagebehandlingRepository.save(
                Klagebehandling(
                    foedselsnummer = oppgaveKopi.ident.folkeregisterident
                        ?: throw RuntimeException("folkeregisterident is missing from oppgave"),
                    tema = oppgaveKopi.tema,
                    frist = oppgaveKopi.fristFerdigstillelse ?: calculateFrist(),
                    sakstype = sakstypeRepository.findByIdOrNull(oppgaveKopi.behandlingstype)
                        ?: throw RuntimeException("No sakstype found for ${oppgaveKopi.id}"),
                    hjemler = hjemmelService.getHjemmelFromOppgaveKopi(oppgaveKopi),
                    tildeltSaksbehandlerident = oppgaveKopi.tilordnetRessurs,
                    oppgavereferanser = listOf(
                        Oppgavereferanse(
                            oppgaveId = oppgaveKopi.id
                        )
                    )
                )
            )
            logger.debug("Created behandling ${createdKlagebehandling.id} for oppgave ${oppgaveKopi.id}")
        }
    }

    fun getKlagebehandlingByOppgaveId(oppgaveId: Long): Klagebehandling {
        return klagebehandlingRepository.findByOppgavereferanserOppgaveId(oppgaveId)
            ?: throw RuntimeException("klagebehandling not found")
    }

    // TODO Implement a proper algorithm
    private fun calculateFrist() = LocalDate.now().plusDays(100)
}
