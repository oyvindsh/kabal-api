package no.nav.klage.oppgave.service.unleash

import no.finn.unleash.strategy.Strategy
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Component

@Component
class ByUserStrategy(private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository) : Strategy {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val PARAM = "user"
    }

    override fun getName(): String = "byUserId"

    override fun isEnabled(parameters: Map<String, String>?): Boolean =
        getEnabledUsers(parameters)?.any { isCurrentUserEnabled(it) } ?: false

    private fun getEnabledUsers(parameters: Map<String, String>?) =
        parameters?.get(PARAM)?.split(',')

    private fun isCurrentUserEnabled(ident: String): Boolean {
        val currentIdent = getIdent()
        logger.debug("isCurrentUserEnabled? ident: {}, currentIdent: {}", ident, currentIdent)
        return ident == currentIdent
    }

    private fun getIdent() = innloggetSaksbehandlerRepository.getInnloggetIdent()
}