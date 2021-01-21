package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.Klagesak
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import no.nav.klage.oppgave.repositories.KlagesakRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager

@Service
@Transactional
class KlagesakService(
    private val klagesakRepository: KlagesakRepository,
    private val kodeverkService: KodeverkService,
    private val hjemmelService: HjemmelService,
    private val entityManager: EntityManager
) {

    fun fetchKlagesakForOppgaveKopi(oppgaveId: Long): Klagesak? {
        val query = entityManager.createQuery("""
            SELECT klagesak_id 
              FROM klage.klage_oppgave
             WHERE oppgave_id = $oppgaveId
        """.trimIndent())

        val idList = query.resultList as List<UUID>

        return when(idList.size) {
            0 -> null
            1 -> klagesakRepository.getOne(idList.first())
            else -> throw RuntimeException("Oppgave $oppgaveId has more than one klagesak ($idList)")
        }
    }

    fun connectOppgaveKopiToKlagesak(oppgaveKopi: OppgaveKopi): UUID {
        val klagesak = fetchKlagesakForOppgaveKopi(oppgaveKopi.id)
        if (klagesak != null) {
            return klagesak.id
        }

        val createdKlage = klagesakRepository.save(Klagesak(
            behandlinger = listOf(Behandling(
                hjemler = hjemmelService.getHjemmelFromOppgaveKopi(oppgaveKopi)
            )),
            foedselsnummer = oppgaveKopi.ident?.verdi ?: throw java.lang.RuntimeException("Missing ident on oppgave ${oppgaveKopi.id}"),
            sakstype = kodeverkService.getSakstypeFromBehandlingstema(oppgaveKopi.behandlingstype)
        ))

        makeConnection(createdKlage.id, oppgaveKopi.id)

        return createdKlage.id
    }

    private fun makeConnection(klagesakId: UUID, oppgaveId: Long) {
        entityManager.createNativeQuery("INSERT INTO klage.klage_oppgave VALUES (?, ?)")
            .setParameter(1, klagesakId)
            .setParameter(2, oppgaveId)
            .executeUpdate()

    }
}
