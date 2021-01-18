package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.axsys.AxsysClient
import no.nav.klage.oppgave.clients.azure.MicrosoftGraphClient
import no.nav.klage.oppgave.clients.fssproxy.KlageProxyClient
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.mapToInterntDomene
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

        private const val LEDER_ROLLE = "0000-GA-GOSYS_VETIKKEHVAROLLENHETER"
        private const val FAGANSVARLIG_ROLLE = "0000-GA-GOSYS_VETIKKEHVAROLLENHETER"
        private const val SAKSBEHANDLER_ROLLE = "0000-GA-GOSYS_OPPGAVE_BEHANDLER"
        private const val KAN_BEHANDLE_STRENGT_FORTROLIG = "0000-GA-GOSYS_KODE6"
        private const val KAN_BEHANDLE_FORTROLIG = "0000-GA-GOSYS_KODE7"
        private const val KAN_BEHANDLE_EGEN_ANSATT = "0000-GA-GOSYS_VETIKKEHVAROLLENHETER"
    }

    fun getTilgangerForSaksbehandler(ident: String): EnheterMedLovligeTemaer =
        axsysClient.getTilgangerForSaksbehandler(ident).mapToInterntDomene()

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

    fun erFagansvarlig(ident: String): Boolean = getRoller(ident).any { it.contains(FAGANSVARLIG_ROLLE) }

    fun erLeder(ident: String): Boolean = getRoller(ident).any { it.contains(LEDER_ROLLE) }

    fun erSaksbehandler(ident: String): Boolean = getRoller(ident).any { it.contains(SAKSBEHANDLER_ROLLE) }

    fun kanBehandleFortrolig(ident: String): Boolean = getRoller(ident).any { it.contains(KAN_BEHANDLE_FORTROLIG) }

    fun kanBehandleStrengtFortrolig(ident: String): Boolean =
        getRoller(ident).any { it.contains(KAN_BEHANDLE_STRENGT_FORTROLIG) }

    fun kanBehandleEgenAnsatt(ident: String): Boolean =
        getRoller(ident).any { it.contains(KAN_BEHANDLE_EGEN_ANSATT) }

    fun getRoller(ident: String): List<String> = klageProxyClient.getRoller(ident)
}