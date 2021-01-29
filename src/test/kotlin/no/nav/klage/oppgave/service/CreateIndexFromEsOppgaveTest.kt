package no.nav.klage.oppgave.service


import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import org.apache.http.util.EntityUtils
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestHighLevelClient
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@ImportAutoConfiguration(ElasticsearchRestClientAutoConfiguration::class, ElasticsearchDataAutoConfiguration::class)
@Disabled("kan brukes for å generere settings og mapping, for så å lagre som fil. Må da endre i ElasticsearchService")
class CreateIndexFromEsOppgaveTest {

    companion object {
        @Container
        @JvmField
        val ES_CONTAINER: ElasticsearchContainer =
            ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:7.9.3")

        @JvmStatic
        @DynamicPropertySource
        fun aivenProperties(registry: DynamicPropertyRegistry) {
            registry.add("AIVEN_ES_HOST", ES_CONTAINER::getHost)
            registry.add("AIVEN_ES_PORT", ES_CONTAINER::getFirstMappedPort)
            registry.add("AIVEN_ES_USERNAME_ADM") { "elastic" }
            registry.add("AIVEN_ES_PASSWORD_ADM") { "changeme" }
            registry.add("AIVEN_ES_SCHEME") { "http" }
            registry.add("AIVEN_ES_USE_SSL") { false }
        }
    }

    @Autowired
    lateinit var esTemplate: ElasticsearchRestTemplate

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