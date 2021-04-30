package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
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
class StatistikkInElasticsearchServiceTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestElasticsearchContainer = TestElasticsearchContainer.instance
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
        assertThat(esContainer.isRunning).isTrue
        service.recreateIndex()
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {

        val indexOps = esTemplate.indexOps(IndexCoordinates.of("klagebehandling"))
        assertThat(indexOps.exists()).isTrue()
    }

    private fun klagebehandling(id: Long, innsendt: LocalDate, frist: LocalDate, avsluttet: LocalDate? = null) =
        EsKlagebehandling(
            id = id.toString(),
            versjon = 1L,
            tildeltEnhet = "4219",
            tema = Tema.OMS.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = innsendt,
            mottattFoersteinstans = LocalDate.of(2018, 11, 1),
            mottattKlageinstans = LocalDate.of(2018, 12, 1),
            frist = frist,
            avsluttet = avsluttet,
            hjemler = listOf()
        )

    @Test
    @Order(3)
    fun `saving klagebehandlinger for later tests`() {


        fun idag() = LocalDate.now()
        fun uviktigdag() = LocalDate.now()

        val klagebehandlinger = listOf(
            klagebehandling(1001L, idag(), idag()),
            klagebehandling(1002L, idag().minusDays(1), idag().minusDays(1)),
            klagebehandling(1003L, idag().minusDays(6), idag().plusDays(1)),
            klagebehandling(1004L, idag().minusDays(7), idag().minusDays(7)),
            klagebehandling(1005L, idag().minusDays(8), idag().plusDays(7)),
            klagebehandling(1006L, idag().minusDays(30), idag().minusDays(30)),
            klagebehandling(1007L, idag().minusDays(31), idag().plusDays(30)),
            klagebehandling(2001L, idag(), idag(), idag()),
            klagebehandling(2002L, idag().minusDays(1), uviktigdag(), idag().minusDays(1)),
            klagebehandling(2003L, idag().minusDays(6), uviktigdag(), idag().minusDays(6)),
            klagebehandling(2004L, idag().minusDays(7), uviktigdag(), idag().minusDays(7)),
            klagebehandling(2005L, idag().minusDays(8), uviktigdag(), idag().minusDays(8)),
            klagebehandling(2006L, idag().minusDays(30), uviktigdag(), idag().minusDays(30)),
            klagebehandling(2007L, idag().minusDays(31), uviktigdag(), idag().minusDays(31)),
        )
        esTemplate.save(klagebehandlinger)

        sleep(2000L)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(14L)
    }

    @Test
    @Order(4)
    fun `Klagebehandling statistikk gives correct numbers`() {
        val statistikkTall = service.statistikkQuery()
        assertThat(statistikkTall.ubehandlede).isEqualTo(7)
        assertThat(statistikkTall.overFrist).isEqualTo(3)
        assertThat(statistikkTall.innsendtIGaar).isEqualTo(2)
        assertThat(statistikkTall.innsendtSiste7Dager).isEqualTo(6)
        assertThat(statistikkTall.innsendtSiste30Dager).isEqualTo(10)
        assertThat(statistikkTall.behandletIGaar).isEqualTo(1)
        assertThat(statistikkTall.behandletSiste7Dager).isEqualTo(3)
        assertThat(statistikkTall.behandletSiste30Dager).isEqualTo(5)
    }


}