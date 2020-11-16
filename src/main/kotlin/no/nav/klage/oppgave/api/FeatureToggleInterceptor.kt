package no.nav.klage.oppgave.api

import no.finn.unleash.Unleash
import no.finn.unleash.UnleashContext
import no.nav.klage.oppgave.config.FeatureToggleConfig.Companion.KLAGE_OPPGAVE_TILGANG
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
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
        val isEnabled = isEnabled(KLAGE_OPPGAVE_TILGANG)
        if (!isEnabled) {
            response.status = HttpStatus.FORBIDDEN.value()
        }
        return isEnabled
    }

    private fun isEnabled(feature: String): Boolean =
        unleash.isEnabled(feature, contextMedInnloggetBruker())

    private fun contextMedInnloggetBruker(): UnleashContext? =
        UnleashContext.builder().userId(getIdent()).build()

    private fun getIdent() = try {
        innloggetSaksbehandlerRepository.getInnloggetIdent()
    } catch (e: Exception) {
        logger.info("Not able to retrieve token", e)
        "UINNLOGGET"
    }
}

@Configuration
class FeatureToggleInterceptorConfig : WebMvcConfigurer {

    @Autowired
    private lateinit var featureToggleInterceptor: FeatureToggleInterceptor

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(featureToggleInterceptor).addPathPatterns("/ansatte/**")
    }
}