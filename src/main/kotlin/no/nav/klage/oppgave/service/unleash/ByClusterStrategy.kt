package no.nav.klage.oppgave.service.unleash

import no.finn.unleash.strategy.Strategy
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ByClusterStrategy(@Value("\${nais.cluster.name}") val currentCluster: String) : Strategy {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val PARAM = "cluster"
    }

    override fun getName(): String = "byCluster"

    override fun isEnabled(parameters: Map<String, String>?): Boolean =
        getEnabledClusters(parameters)?.any { isCurrentClusterEnabled(it) } ?: false

    private fun getEnabledClusters(parameters: Map<String, String>?) =
        parameters?.get(PARAM)?.split(',')

    private fun isCurrentClusterEnabled(cluster: String): Boolean {
        return currentCluster == cluster
    }
}