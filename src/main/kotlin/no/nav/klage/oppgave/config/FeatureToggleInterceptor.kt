package no.nav.klage.oppgave.config

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.config.FeatureToggleConfig.Companion.KLAGE_GENERELL_TILGANG
import no.nav.klage.oppgave.exceptions.FeatureNotEnabledException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class FeatureToggleInterceptor(
    private val unleash: Unleash,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) : HandlerInterceptorAdapter() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Throws(Exception::class)
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any?
    ): Boolean {
        val isEnabled = isEnabled(KLAGE_GENERELL_TILGANG)
        if (!isEnabled) {
            throw FeatureNotEnabledException("Du er ikke gitt tilgang til klage-oppgave-api")
        }
        return isEnabled
    }

    private fun isEnabled(feature: String): Boolean {
        logger.debug("Unleash: feature: {}", feature)
        val contextMedInnloggetBruker = contextMedInnloggetBruker()

        unleash as DefaultUnleash
        val featureToggleDefinition = unleash.getFeatureToggleDefinition(feature)

        featureToggleDefinition.get().strategies?.forEach {
            logger.debug("strategy: {}", it.name)
            it.constraints?.forEach { c ->
                logger.debug("  name: {}", c.contextName)
                logger.debug("  operator: {}", c.operator)
                c.values?.forEach { v ->
                    logger.debug("    value: {}", v)
                }
            }
        }

        return unleash.isEnabled(feature, contextMedInnloggetBruker)
    }

    private fun contextMedInnloggetBruker(): UnleashContext? {
        val ident = getIdent()
        logger.debug("Unleash: getIdent(): {}", ident)
        return UnleashContext.builder().userId(ident).build()
    }

    private fun getIdent() = innloggetSaksbehandlerRepository.getInnloggetIdent()
}

@Configuration
class FeatureToggleInterceptorConfig(private val featureToggleInterceptor: FeatureToggleInterceptor) :
    WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(featureToggleInterceptor).addPathPatterns("/ansatte/**")
    }
}
