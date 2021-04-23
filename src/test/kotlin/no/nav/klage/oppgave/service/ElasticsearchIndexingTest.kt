package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
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
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.lang.Thread.sleep
import java.time.LocalDate


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@ImportAutoConfiguration(
    ElasticsearchRestClientAutoConfiguration::class,
    ElasticsearchDataAutoConfiguration::class
)
class ElasticsearchIndexingTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestElasticsearchContainer = TestElasticsearchContainer.instance
    }

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var esTemplate: ElasticsearchRestTemplate

    @Autowired
    lateinit var service: ElasticsearchService

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(esContainer.isRunning).isTrue

    }

    @Test
    @Order(2)
    fun `index has been created by service`() {
        val indexOps = esTemplate.indexOps(IndexCoordinates.of("klagebehandling"))
        assertThat(indexOps.exists()).isTrue
        service.recreateIndex()
    }

    @Test
    @Order(3)
    fun `klagebehandling can be saved and retrieved`() {

        val klagebehandling = klagebehandlingWith(
            id = "1001L",
            versjon = 1L,
            saksreferanse = "hei"
        )
        esTemplate.save(klagebehandling)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.saksreferanse).isEqualTo("hei")
    }

    @Test
    @Order(4)
    fun `klagebehandling can be saved twice without creating a duplicate`() {

        var klagebehandling = klagebehandlingWith(
            id = "2001L",
            versjon = 1L,
            saksreferanse = "hei"
        )
        esTemplate.save(klagebehandling)

        klagebehandling = klagebehandlingWith(
            id = "2001L",
            versjon = 2L,
            saksreferanse = "hallo"
        )
        esTemplate.save(klagebehandling)
        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("2001L"))
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.saksreferanse).isEqualTo("hallo")
    }

    @Test
    @Order(5)
    fun `saving an earlier version of klagebehandling causes a conflict`() {

        var klagebehandling = klagebehandlingWith(
            id = "3001L",
            versjon = 2L,
            saksreferanse = "hei"
        )
        esTemplate.save(klagebehandling)

        klagebehandling = klagebehandlingWith(
            id = "3001L",
            versjon = 1L,
            saksreferanse = "hallo"
        )
        assertThatThrownBy {
            esTemplate.save(klagebehandling)
        }.isInstanceOf(UncategorizedElasticsearchException::class.java)
            .hasRootCauseInstanceOf(ElasticsearchStatusException::class.java)
            .hasMessageContaining("type=version_conflict_engine_exception")

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.idsQuery().addIds("3001L"))
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(1L)
        assertThat(searchHits.searchHits.first().content.saksreferanse).isEqualTo("hei")
    }

    private fun klagebehandlingWith(id: String, versjon: Long, saksreferanse: String): EsKlagebehandling {
        return EsKlagebehandling(
            id = id,
            versjon = versjon,
            journalpostId = emptyList(),
            saksreferanse = saksreferanse,
            tildeltEnhet = "",
            tema = Tema.SYK,
            type = Type.KLAGE,
            tildeltSaksbehandlerident = null,
            innsendt = null,
            mottattFoersteinstans = null,
            mottattKlageinstans = LocalDate.now(),
            frist = null,
            startet = null,
            avsluttet = null,
            hjemler = listOf(Hjemmel.FTL_8_35.id),
            foedselsnummer = null,
            navn = null,
            egenAnsatt = false,
            fortrolig = false,
            strengtFortrolig = false

        )
    }
}