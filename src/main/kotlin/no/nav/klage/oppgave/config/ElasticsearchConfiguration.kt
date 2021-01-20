package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.http.HttpHeaders
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class ElasticsearchConfiguration(
    @Value("\${AIVEN_ES_HOST}") val host: String,
    @Value("\${AIVEN_ES_PORT}") val port: String,
    @Value("\${AIVEN_ES_USERNAME_READ}") val usernameRead: String,
    @Value("\${AIVEN_ES_PASSWORD_READ}") val passwordRead: String,
    @Value("\${AIVEN_ES_USERNAME_WRITE}") val usernameWrite: String,
    @Value("\${AIVEN_ES_PASSWORD_WRITE}") val passwordWrite: String,
    @Value("\${AIVEN_ES_USERNAME_ADM}") val usernameAdmin: String,
    @Value("\${AIVEN_ES_PASSWORD_ADM}") val passwordAdmin: String
) : AbstractElasticsearchConfiguration() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {

        val clientConfiguration: ClientConfiguration = ClientConfiguration.builder()
            .connectedTo("$host:$port")
            .usingSsl()
            .withConnectTimeout(Duration.ofSeconds(5))
            .withSocketTimeout(Duration.ofSeconds(3))
            .withBasicAuth(usernameAdmin, passwordAdmin)
            .withHeaders {
                val headers = HttpHeaders()
                headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                headers
            }
            .build();

        securelogger.info("Kobler til ES $host:$port med $usernameAdmin")
        securelogger.info("Kobler til ES endpoint ${clientConfiguration.endpoints}")
        securelogger.info("Kobler til ES med ssl ${clientConfiguration.useSsl()}")

        return RestClients.create(clientConfiguration).rest();
    }
} 