package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.repositories.SakstypeRepository
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class KodeverkService(
    private val sakstypeRepository: SakstypeRepository
) {

    fun getSakstypeFromBehandlingstema(behandlingstype: String?) = when(behandlingstype) {
        "ae0058" -> sakstypeRepository.getOne(1)
        "ae0046" -> sakstypeRepository.getOne(2)
        else -> throw RuntimeException("Unkown behandlinstype")
    }

}
