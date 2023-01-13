package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service

@Service
class EnhetService(
    private val behandlingService: BehandlingService,
    private val kabalInnstillingerService: KabalInnstillingerService
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun getAllRelevantYtelserForEnhet(enhet: String): List<String> {
        val enhetensTildelteYtelser = kabalInnstillingerService.getTildelteYtelserForEnhet(enhet)
        val enhetensBehandlingerYtelseSet =
            behandlingService.getAllBehandlingerForEnhet(enhet).map { it.ytelse }.toSet()

        return (enhetensTildelteYtelser + enhetensBehandlingerYtelseSet).sortedBy { it.navn }.map { it.id }
    }
}