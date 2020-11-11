package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.AxsysClient
import no.nav.klage.oppgave.clients.KlageProxyClient
import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SaksbehandlerRepository(
    private val client: MicrosoftGraphClient,
    private val axsysClient: AxsysClient,
    private val klageProxyClient: KlageProxyClient
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()

        const val MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY = 15

        private const val LEDER_ROLLE = "?"
        private const val FAGANSVARLIG_ROLLE = "?"
    }

    fun getTilgangerForSaksbehandler(ident: String) =
        axsysClient.getTilgangerForSaksbehandler(ident)

    fun getNamesForSaksbehandlere(identer: Set<String>): Map<String, String> {
        logger.debug("Fetching names for saksbehandlere from Microsoft Graph")

        val identerNotInCache = identer.toMutableSet()
        identerNotInCache -= saksbehandlerNameCache.keys
        logger.debug("Only fetching identer not in cache: {}", identerNotInCache)

        val chunkedList = identerNotInCache.chunked(MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY)

        val measuredTimeMillis = measureTimeMillis {
            saksbehandlerNameCache += client.getAllDisplayNames(chunkedList)
        }
        logger.debug("It took {} millis to fetch all names", measuredTimeMillis)

        return saksbehandlerNameCache
    }

    fun erFagansvarlig(ident: String): Boolean {
        return klageProxyClient.getRoller(ident).contains(FAGANSVARLIG_ROLLE)
    }

    fun erLeder(ident: String): Boolean {
        return klageProxyClient.getRoller(ident).contains(LEDER_ROLLE)
    }
}