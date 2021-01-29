package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.elasticsearch.Prioritet
import no.nav.klage.oppgave.domain.elasticsearch.Status
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.elasticsearch.ElasticsearchStatusException
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
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException
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
import java.time.LocalDateTime


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@ImportAutoConfiguration(ElasticsearchRestClientAutoConfiguration::class, ElasticsearchDataAutoConfiguration::class)
class ElasticsearchIndexingTest {

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

    @MockkBean
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var esTemplate: ElasticsearchRestTemplate

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(ES_CONTAINER.isRunning).isTrue
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {
        val indexOps = esTemplate.indexOps(IndexCoordinates.of("oppgavekopier"))
        assertThat(indexOps.exists()).isTrue
    }

    @Test
    @Order(3)
    fun `oppgave can be saved and retrieved`() {

        val oppgave = oppgaveWith(
            id = 1001L,
            versjon = 1L,
            beskrivelse = "hei"
        )
        esTemplate.save(oppgave)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.beskrivelse).isEqualTo("hei")
    }

    @Test
    @Order(4)
    fun `oppgave can be saved twice without creating a duplicate`() {

        var oppgave = oppgaveWith(
            id = 2001L,
            versjon = 1L,
            beskrivelse = "hei"
        )
        esTemplate.save(oppgave)

        oppgave = oppgaveWith(
            id = 2001L,
            versjon = 2L,
            beskrivelse = "hallo"
        )
        esTemplate.save(oppgave)
        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("2001"))
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.beskrivelse).isEqualTo("hallo")
    }

    @Test
    @Order(5)
    fun `saving an earlier version of oppgave causes a conflict`() {

        var oppgave = oppgaveWith(
            id = 3001L,
            versjon = 2L,
            beskrivelse = "hei"
        )
        esTemplate.save(oppgave)

        oppgave = oppgaveWith(
            id = 3001L,
            versjon = 1L,
            beskrivelse = "hallo"
        )
        assertThatThrownBy {
            esTemplate.save(oppgave)
        }.isInstanceOf(UncategorizedElasticsearchException::class.java)
            .hasRootCauseInstanceOf(ElasticsearchStatusException::class.java)
            .hasMessageContaining("type=version_conflict_engine_exception")

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("3001"))
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.beskrivelse).isEqualTo("hei")
    }

    private fun oppgaveWith(id: Long, versjon: Long, beskrivelse: String): EsOppgave {
        return EsOppgave(
            id = id,
            version = versjon,
            tema = "SYK",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "BEH_SAK_MK",
            behandlingstype = "ae0058",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.now(),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.now(),
            beskrivelse = beskrivelse
        )
    }
}