package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsOppgave
import no.nav.klage.oppgave.domain.elasticsearch.Prioritet
import no.nav.klage.oppgave.domain.elasticsearch.Status
import no.nav.klage.oppgave.domain.elasticsearch.Statuskategori
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.elasticsearch.ElasticsearchContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.lang.Thread.sleep
import java.time.LocalDate
import java.time.LocalDateTime


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@ExtendWith(SpringExtension::class)
@ContextConfiguration(
    initializers = [ElasticsearchRepositoryTest.Companion.Initializer::class],
    classes = [ElasticsearchServiceConfiguration::class]
)
class ElasticsearchRepositoryTest {

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

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var repository: ElasticsearchRepository

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
    fun `index has been created by service`() {

        val indexOps = esTemplate.indexOps(IndexCoordinates.of("oppgavekopier"))
        assertThat(indexOps.exists()).isTrue()
    }

    @Test
    @Order(3)
    fun `lagrer to oppgaver for senere tester`() {

        val oppgave1 = EsOppgave(
            id = 1001L,
            version = 1L,
            tema = "SYK",
            status = Status.OPPRETTET,
            tildeltEnhetsnr = "4219",
            oppgavetype = "BEH_SAK_MK",
            behandlingstype = "ae0058",
            prioritet = Prioritet.NORM,
            fristFerdigstillelse = LocalDate.of(2020, 12, 1),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.of(2020, 12, 1, 20, 15),
            beskrivelse = "beskrivelse",
            statuskategori = Statuskategori.AAPEN
        )
        val oppgave2 = EsOppgave(
            id = 1002L,
            version = 1L,
            tema = "FOR",
            status = Status.AAPNET,
            tildeltEnhetsnr = "4220",
            oppgavetype = "BEH_SAK",
            behandlingstype = "ae0058",
            prioritet = Prioritet.HOY,
            fristFerdigstillelse = LocalDate.of(2019, 12, 1),
            aktivDato = LocalDate.now(),
            opprettetAv = "H149290",
            opprettetTidspunkt = LocalDateTime.of(2019, 12, 1, 20, 15),
            beskrivelse = "beskrivelse",
            statuskategori = Statuskategori.AAPEN
        )
        esTemplate.save(oppgave1)
        esTemplate.save(oppgave2)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsOppgave> = esTemplate.search(query, EsOppgave::class.java)
        assertThat(searchHits.totalHits).isEqualTo(2L)
    }

    @Test
    @Order(4)
    fun `oppgave can be searched for by tema`() {
        val oppgaver: List<EsOppgave> =
            repository.findByCriteria(
                OppgaverSearchCriteria(
                    temaer = listOf("SYK"),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(oppgaver.size).isEqualTo(1L)
        assertThat(oppgaver.first().id).isEqualTo(1001L)
    }

    @Test
    @Order(5)
    fun `oppgave can be searched for by frist`() {
        val oppgaver: List<EsOppgave> =
            repository.findByCriteria(
                OppgaverSearchCriteria(
                    fristFom = LocalDate.of(2020, 12, 1),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(oppgaver.size).isEqualTo(1L)
        assertThat(oppgaver.first().id).isEqualTo(1001L)
    }

}