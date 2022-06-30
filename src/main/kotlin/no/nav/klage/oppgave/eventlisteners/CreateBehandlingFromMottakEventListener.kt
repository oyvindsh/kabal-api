package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.events.MottakLagretEvent
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.service.AnkebehandlingService
import no.nav.klage.oppgave.service.KlagebehandlingService
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class CreateBehandlingFromMottakEventListener(
    private val klagebehandlingService: KlagebehandlingService,
    private val ankebehandlingService: AnkebehandlingService,
    private val behandlingRepository: BehandlingRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @EventListener
    fun createBehandling(mottakLagretEvent: MottakLagretEvent): Behandling {
        logger.debug("Received MottakLagretEvent for mottak ${mottakLagretEvent.mottak.id} in CreateKlagebehandlingFromMottakEventListener")
        val mottakId = mottakLagretEvent.mottak.id
        //TODO
//        if (behandlingRepository.findByMottakId(mottakId) != null) {
//            logger.error("We already have a behandling for mottak ${mottakId}. This is not supposed to happen.")
//            throw RuntimeException("We already have a behandling for mottak $mottakId")
//        }

        return when (mottakLagretEvent.mottak.type) {
            Type.KLAGE -> klagebehandlingService.createKlagebehandlingFromMottak(mottakLagretEvent.mottak)
            Type.ANKE -> ankebehandlingService.createAnkebehandlingFromMottak(mottakLagretEvent.mottak)
            Type.ANKE_I_TRYGDERETTEN -> TODO()
        }
    }
}