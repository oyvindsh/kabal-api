package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class OppgaveClientConfiguration(private val webClientBuilder: WebClient.Builder) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${OPPGAVE_URL}")
    private lateinit var oppgaveServiceURL: String

    @Value("\${OPPGAVE_APIKEY}")
    private lateinit var apiKey: String

    @Bean
    fun oppgaveWebClient(): WebClient {
        return webClientBuilder
            .defaultHeader("x-nav-apiKey", apiKey)
            .defaultHeader("X-Correlation-ID", "klage1234")
            .baseUrl(oppgaveServiceURL)
            .build()
    }
}