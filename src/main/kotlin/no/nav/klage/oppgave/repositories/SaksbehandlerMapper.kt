package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.axsys.Tilganger
import no.nav.klage.oppgave.domain.kodeverk.LovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.saksbehandler.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.util.getLogger
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SaksbehandlerMapper(environment: Environment) {

    private val lovligeTemaerIKabal = LovligeTemaer.lovligeTemaer(environment)

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun mapTilgangerToEnheterMedLovligeTemaer(tilganger: Tilganger): EnheterMedLovligeTemaer {

        return EnheterMedLovligeTemaer(tilganger.enheter.map { enhet ->
            EnhetMedLovligeTemaer(
                enhet.enhetId,
                enhet.navn,
                enhet.temaer?.mapNotNull { mapTemaNavnToTema(it) }?.filter { lovligeTemaerIKabal.contains(it) }
                    ?: emptyList())
        })
    }

    private fun mapTemaNavnToTema(tema: String): Tema? =
        try {
            Tema.fromNavn(tema)
        } catch (e: Exception) {
            logger.warn("Unable to map Tema $tema. Ignoring and moving on", e)
            null
        }
}