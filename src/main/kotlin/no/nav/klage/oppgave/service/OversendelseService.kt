package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.repositories.MottakRepository
import org.springframework.stereotype.Service

@Service
class OversendelseService(
    private val mottakRepository: MottakRepository
) {

    fun createMottakForKlage(oversendtKlage: OversendtKlage) {
        mottakRepository.save(oversendtKlage.toMottak())
    }

}
