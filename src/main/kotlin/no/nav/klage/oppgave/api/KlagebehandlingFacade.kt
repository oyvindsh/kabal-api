package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.api.view.KlagebehandlingView
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class KlagebehandlingFacade(
    private val klagebehandlingMapper: KlagebehandlingMapper,
    private val klagebehandlingService: KlagebehandlingService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun getKlagebehandling(oppgaveId: Long): KlagebehandlingView {
        return klagebehandlingMapper.mapKlagebehandlingToKlagebehandlingView(
            klagebehandlingService.getKlagebehandlingByOppgaveId(
                oppgaveId
            )
        )
    }
}