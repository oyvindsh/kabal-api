package no.nav.klage.oppgave.config

import io.netty.handler.logging.LogLevel
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.transport.logging.AdvancedByteBufFormat

@Configuration
class AxsysClientConfiguration(
    private val webClientBuilder: WebClient.Builder
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Value("\${AXSYS_APIKEY}")
    private lateinit var apiKey: String

    @Value("\${AXSYS_URL}")
    private lateinit var axsysServiceURL: String

    @Bean
    fun axsysWebClient(): WebClient {

        logger.info("Setting up axsys webclient with base url $axsysServiceURL")
        
        val httpClient = HttpClient
            .create()
            .wiretap("reactor.netty.http.client.HttpClient", LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);

        return webClientBuilder
            .defaultHeader("x-nav-apiKey", apiKey)
            .baseUrl(axsysServiceURL)
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .build()
    }
}