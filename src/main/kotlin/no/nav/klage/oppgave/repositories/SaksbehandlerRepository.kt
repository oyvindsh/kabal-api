package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.clients.axsys.AxsysClient
import no.nav.klage.oppgave.clients.axsys.Tilganger
import no.nav.klage.oppgave.clients.azure.MicrosoftGraphClient
import no.nav.klage.oppgave.domain.EnhetMedLovligeTemaer
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

@Service
class SaksbehandlerRepository(
    private val client: MicrosoftGraphClient,
    private val axsysClient: AxsysClient,
    @Value("\${ROLE_GOSYS_OPPGAVE_BEHANDLER}") private val gosysSaksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_SAKSBEHANDLER}") private val saksbehandlerRole: String,
    @Value("\${ROLE_KLAGE_FAGANSVARLIG}") private val fagansvarligRole: String,
    @Value("\${ROLE_KLAGE_LEDER}") private val lederRole: String,
    @Value("\${ROLE_KLAGE_MERKANTIL}") private val merkantilRole: String,
    @Value("\${ROLE_KLAGE_FORTROLIG}") private val kanBehandleFortroligRole: String,
    @Value("\${ROLE_KLAGE_STRENGT_FORTROLIG}") private val kanBehandleStrengtFortroligRole: String,
    @Value("\${ROLE_KLAGE_EGEN_ANSATT}") private val kanBehandleEgenAnsattRole: String
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)

        val saksbehandlerNameCache = mutableMapOf<String, String>()

        const val MAX_AMOUNT_IDENTS_IN_GRAPH_QUERY = 15
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

    fun erFagansvarlig(ident: String): Boolean = getRoller(ident).hasRole(fagansvarligRole)

    fun erLeder(ident: String): Boolean = getRoller(ident).hasRole(lederRole)

    fun erSaksbehandler(ident: String): Boolean =
        getRoller(ident).hasRole(saksbehandlerRole)
                || getRoller(ident).hasRole(gosysSaksbehandlerRole)

    fun kanBehandleFortrolig(ident: String): Boolean = getRoller(ident).hasRole(kanBehandleFortroligRole)

    fun kanBehandleStrengtFortrolig(ident: String): Boolean =
        getRoller(ident).hasRole(kanBehandleStrengtFortroligRole)

    fun kanBehandleEgenAnsatt(ident: String): Boolean = getRoller(ident).hasRole(kanBehandleEgenAnsattRole)

    private fun getRoller(ident: String): List<String> = client.getRoller(ident)

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }

    private fun Tilganger.mapToInterntDomene(): EnheterMedLovligeTemaer =
        EnheterMedLovligeTemaer(this.enheter.map { enhet ->
            EnhetMedLovligeTemaer(
                enhet.enhetId,
                enhet.navn,
                enhet.temaer?.mapNotNull { mapTemaNavnToTema(it) } ?: emptyList())
        })

    private fun mapTemaNavnToTema(tema: String): Tema? =
        try {
            Tema.fromNavn(tema)
        } catch (e: Exception) {
            logger.error("Unable to map Tema $tema", e)
            null
        }
}