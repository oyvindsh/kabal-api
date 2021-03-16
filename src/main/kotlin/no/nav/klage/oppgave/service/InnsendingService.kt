package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.InnsendtKlage
import no.nav.klage.oppgave.repositories.MottakRepository
import org.springframework.stereotype.Service

@Service
class InnsendingService(
    private val mottakRepository: MottakRepository
) {

    fun validerInnsending(innsendtKlage: InnsendtKlage): String? {
        return null
    }

    fun createMottakForKlage(innsendtKlage: InnsendtKlage) {
        mottakRepository.save(innsendtKlage.toMottak())
    }

}
