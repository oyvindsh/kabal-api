package no.nav.klage.oppgave.service


import no.nav.klage.oppgave.config.ElasticsearchConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import org.apache.http.util.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    initializers = [CreateIndexFromEsOppgaveTest.Companion.Initializer::class],
    classes = [ElasticsearchConfiguration::class]
)
@Disabled("kan brukes for å generere settings og mapping, for så å lagre som fil. Må da endre i ElasticsearchService")
class CreateIndexFromEsOppgaveTest {

    companion object {
        @Container
        @JvmField
        val ES_CONTAINER: ElasticsearchContainer =
            ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.9.3")

        class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
            override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {

                TestPropertyValues.of(
                    "AIVEN_ES_HOST=${ES_CONTAINER.host}",
                    "AIVEN_ES_PORT=${ES_CONTAINER.firstMappedPort}",
                    "AIVEN_ES_USERNAME_ADM=elastic",
                    "AIVEN_ES_PASSWORD_ADM=changeme",
                ).applyTo(configurableApplicationContext.environment)
            }
        }
    }

    @Autowired
    lateinit var esTemplate: ElasticsearchOperations

    @Autowired
    lateinit var client: RestHighLevelClient

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(ES_CONTAINER.isRunning).isTrue
    }

    @Test
    @Order(2)
    fun `denne vil printe ut settings og mapping generert fra EsOppgave`() {

        val indexOps = esTemplate.indexOps(EsOppgave::class.java)
        indexOps.create()
        val mappingDocument = indexOps.createMapping(EsOppgave::class.java)
        indexOps.putMapping(mappingDocument)

        val mappingResponse = client.lowLevelClient.performRequest(Request("GET", "/_all/_mapping"))
        val mapping: String = EntityUtils.toString(mappingResponse.entity)
        println(mapping)
        println(mappingDocument.toJson())
        val settingsResponse = client.lowLevelClient.performRequest(Request("GET", "/_all/_settings"))
        val settings: String = EntityUtils.toString(settingsResponse.entity)
        println(settings)
    }
}