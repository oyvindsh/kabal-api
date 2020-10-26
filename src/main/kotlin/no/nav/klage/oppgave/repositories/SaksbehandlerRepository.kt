package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.MicrosoftGraphClient
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SaksbehandlerRepository(val client: MicrosoftGraphClient) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()

        const val MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY = 15
    }

    fun getNavIdent(accessToken: String): String =
        client.getNavIdent(accessToken)

    fun getNamesForSaksbehandlere(identer: Set<String>, accessToken: String): Map<String, String> {
        logger.debug("Fetching names for saksbehandlere from Microsoft Graph")

        val identerNotInCache = identer.toMutableSet()
        identerNotInCache -= saksbehandlerNameCache.keys
        logger.debug("Only fetching identer not in cache: {}", identerNotInCache)

        val chunkedList = identerNotInCache.chunked(MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY)

        val measuredTimeMillis = measureTimeMillis {
            chunkedList.forEach {
                saksbehandlerNameCache += client.getDisplayNames(it, accessToken)
            }
        }
        logger.debug("It took {} millis to fetch all names", measuredTimeMillis)

        return saksbehandlerNameCache
    }
}