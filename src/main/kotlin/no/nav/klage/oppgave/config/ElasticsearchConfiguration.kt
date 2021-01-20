package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.http.HttpStatus
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestHighLevelClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.json.JsonParser
import org.springframework.boot.json.JsonParserFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.RestClients
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration
import org.springframework.http.HttpHeaders
import org.springframework.util.StreamUtils
import java.nio.charset.StandardCharsets
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
    @Value("\${AIVEN_ES_PASSWORD_ADM}") val passwordAdmin: String,
    @Autowired private val environment: Environment
) : AbstractElasticsearchConfiguration() {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    @Bean
    override fun elasticsearchClient(): RestHighLevelClient {

        val local = environment.activeProfiles.contains("local")
        val clientConfiguration: ClientConfiguration = if (local) {
            localClientConfiguration()
        } else {
            gcpClientConfiguration()
        }.build();

        securelogger.info("Kobler til ES $host:$port med $usernameAdmin")
        securelogger.info("Kobler til ES endpoint ${clientConfiguration.endpoints}")
        securelogger.info("Kobler til ES med ssl ${clientConfiguration.useSsl()}")

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    fun healthIndicator() = ElasticsearchRestHealthIndicator(elasticsearchClient())

    fun localClientConfiguration() = ClientConfiguration.builder()
        .connectedTo("$host:$port")
        .withConnectTimeout(Duration.ofSeconds(5))
        .withSocketTimeout(Duration.ofSeconds(3))
        .withBasicAuth(usernameAdmin, passwordAdmin)
        .withHeaders {
            val headers = HttpHeaders()
            headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            headers
        }

    fun gcpClientConfiguration() = ClientConfiguration.builder()
        .connectedTo("$host:$port")
        //.usingSsl()
        .withConnectTimeout(Duration.ofSeconds(5))
        .withSocketTimeout(Duration.ofSeconds(3))
        .withBasicAuth(usernameAdmin, passwordAdmin)
        .withHeaders {
            val headers = HttpHeaders()
            headers.add("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            headers
        }
}


class ElasticsearchRestHealthIndicator(private val client: RestHighLevelClient) :
    AbstractHealthIndicator("Elasticsearch health check failed") {
    private val jsonParser: JsonParser = JsonParserFactory.getJsonParser()

    @Throws(Exception::class)
    override fun doHealthCheck(builder: Health.Builder) {

        val response = client.lowLevelClient.performRequest(Request("GET", "/_cluster/health/"))
        val statusLine = response.statusLine
        if (statusLine.statusCode != HttpStatus.SC_OK) {
            builder.down()
            builder.withDetail("statusCode", statusLine.statusCode)
            builder.withDetail("reasonPhrase", statusLine.reasonPhrase)
            return
        }
        response.entity.content.use { inputStream ->
            doHealthCheck(
                builder,
                StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8)
            )
        }
    }

    private fun doHealthCheck(builder: Health.Builder, json: String) {
        val response = jsonParser.parseMap(json)
        val status = response["status"] as String?
        if (RED_STATUS == status) {
            builder.outOfService()
        } else {
            builder.up()
        }
        builder.withDetails(response)
    }

    companion object {
        private const val RED_STATUS = "red"
    }

}