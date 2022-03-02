package no.nav.klage.oppgave.service.distribusjon

import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KlagebehandlingDistribusjonService(
    private val behandlingAvslutningService: BehandlingAvslutningService,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional(propagation = Propagation.NEVER)
    fun avsluttBehandling(behandlingId: UUID) {
        try {
            val hovedDokumenterIkkeFerdigstilte =
                dokumentUnderArbeidRepository.findByMarkertFerdigNotNullAndFerdigstiltNullAndParentIdIsNull()
            if (hovedDokumenterIkkeFerdigstilte.isNotEmpty()) {
                logger.warn("Kunne ikke avslutte behandling $behandlingId fordi noen dokumenter mangler ferdigstilling. Prøver på nytt senere.")
                return
            }

            logger.debug("Alle vedtak i behandling $behandlingId er ferdigstilt, så vi markerer behandlingen som avsluttet")
            behandlingAvslutningService.avsluttBehandling(behandlingId)

        } catch (e: Exception) {
            logger.error("Feilet under avslutning av behandling $behandlingId. Se mer i secure log")
            secureLogger.error("Feilet under avslutning av behandling $behandlingId", e)
        }
    }
}