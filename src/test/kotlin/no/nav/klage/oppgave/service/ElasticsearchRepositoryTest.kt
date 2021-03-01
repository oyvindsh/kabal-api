package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.lang.Thread.sleep
import java.time.LocalDate


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@ImportAutoConfiguration(ElasticsearchRestClientAutoConfiguration::class, ElasticsearchDataAutoConfiguration::class)
class ElasticsearchRepositoryTest {

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

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var repository: ElasticsearchRepository

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
    fun `index has been created by service`() {

        val indexOps = esTemplate.indexOps(IndexCoordinates.of("klagebehandling"))
        assertThat(indexOps.exists()).isTrue()
    }

    @Test
    @Order(3)
    fun `lagrer to oppgaver for senere tester`() {


        val klagebehandling1 = EsKlagebehandling(
            id = "1001L",
            versjon = 1L,
            tildeltEnhet = "4219",
            tema = Tema.SYK,
            sakstype = Sakstype.KLAGE,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2019, 10, 1),
            mottattFoersteinstans = LocalDate.of(2019, 11, 1),
            mottattKlageinstans = LocalDate.of(2019, 12, 1),
            frist = LocalDate.of(2020, 12, 1),
            hjemler = listOf()
        )
        val klagebehandling2 =
            EsKlagebehandling(
                id = "1002L",
                versjon = 1L,
                tildeltEnhet = "4219",
                tema = Tema.FOR,
                sakstype = Sakstype.KLAGE,
                tildeltSaksbehandlerident = null,
                innsendt = LocalDate.of(2018, 10, 1),
                mottattFoersteinstans = LocalDate.of(2018, 11, 1),
                mottattKlageinstans = LocalDate.of(2018, 12, 1),
                frist = LocalDate.of(2019, 12, 1),
                hjemler = listOf()
            )
        esTemplate.save(klagebehandling1)
        esTemplate.save(klagebehandling2)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(2L)
    }

    @Test
    @Order(4)
    fun `Klagebehandling can be searched for by tema`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            repository.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.SYK),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.first().id).isEqualTo("1001L")
    }

    @Test
    @Order(5)
    fun `Klagebehandling can be searched for by frist`() {
        val klagebehandlinger: List<EsKlagebehandling> =
            repository.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    fristFom = LocalDate.of(2020, 12, 1),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.size).isEqualTo(1L)
        assertThat(klagebehandlinger.first().id).isEqualTo("1001L")
    }

}