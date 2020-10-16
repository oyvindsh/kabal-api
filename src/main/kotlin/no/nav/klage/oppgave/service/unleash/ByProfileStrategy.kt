package no.nav.klage.oppgave.service.unleash

import no.finn.unleash.strategy.Strategy
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class ByProfileStrategy(env: Environment) : Strategy {

    companion object {
        val PROFILES = listOf("dev-gcp", "prod-gcp")
    }

    private val currentProfile = env.activeProfiles.find { PROFILES.contains(it) } ?: "dev-gcp"

    override fun getName(): String = "byProfile"

    override fun isEnabled(parameters: Map<String, String>?): Boolean =
        getEnabledProfiles(parameters)?.any { isCurrentProfileEnabled(it) } ?: false

    private fun getEnabledProfiles(parameters: Map<String, String>?) =
        parameters?.get("profile")?.split(',')

    private fun isCurrentProfileEnabled(profile: String): Boolean =
        currentProfile == profile
}