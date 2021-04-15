package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
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
class RelatedKlagebehandlingerTest {

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
    lateinit var service: ElasticsearchService

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

    private fun klagebehandling(
        id: Long,
        fnr: String,
        saksreferanse: String?,
        journalpostIder: List<String>,
        aapen: Boolean
    ) =
        EsKlagebehandling(
            id = id.toString(),
            versjon = 1L,
            tildeltEnhet = "4219",
            tema = Tema.FOR,
            sakstype = Sakstype.KLAGE,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.now(),
            mottattFoersteinstans = LocalDate.now(),
            mottattKlageinstans = LocalDate.now(),
            frist = LocalDate.now(),
            avsluttet = if (aapen) {
                null
            } else {
                LocalDate.now()
            },
            hjemler = listOf(),
            foedselsnummer = fnr,
            saksreferanse = saksreferanse,
            journalpostId = journalpostIder
        )

    @Test
    @Order(3)
    fun `saving klagebehandlinger for later tests`() {

        val klagebehandlinger = listOf(
            klagebehandling(1001L, "01019012345", "AAA123", listOf(), true),
            klagebehandling(1002L, "02019012345", "AAA123", listOf(), false),
            klagebehandling(1003L, "03019012345", "BBB123", listOf(), true),
            klagebehandling(1004L, "01019012345", "BBB123", listOf(), false),
            klagebehandling(1005L, "02019012345", "CCC123", listOf("111222", "333444"), true),
            klagebehandling(1006L, "03019012345", "CCC123", listOf("333444", "555666"), false)
        )
        esTemplate.save(klagebehandlinger)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(6L)
    }

    @Test
    @Order(4)
    fun `related klagebehandlinger gives correct answer`() {
        val related = service.findRelatedKlagebehandlinger("01019012345", "AAA123", listOf("333444", "777888"))
        assertThat(related.aapneByFnr.map { it.id }).containsExactly("1001")
        assertThat(related.avsluttedeByFnr.map { it.id }).containsExactly("1004")
        assertThat(related.aapneBySaksreferanse.map { it.id }).containsExactly("1001")
        assertThat(related.avsluttedeBySaksreferanse.map { it.id }).containsExactly("1002")
        assertThat(related.aapneByJournalpostid.map { it.id }).containsExactly("1005")
        assertThat(related.avsluttedeByJournalpostid.map { it.id }).containsExactly("1006")
    }


}