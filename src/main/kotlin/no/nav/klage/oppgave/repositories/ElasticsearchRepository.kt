package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.OppgaverSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.util.getLogger
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import java.time.format.DateTimeFormatter


open class ElasticsearchRepository(
    private val esTemplate: ElasticsearchOperations,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
) :
    ApplicationListener<ContextRefreshedEvent> {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        try {
            logger.info("Trying to initialize Elasticsearch")
            val indexOps = esTemplate.indexOps(IndexCoordinates.of("klagebehandling"))
            logger.info("Does klagebehandling exist in Elasticsearch?")
            if (!indexOps.exists()) {
                logger.info("klagebehandling does not exist in Elasticsearch")
                indexOps.create(readFromfile("settings.json"))
                indexOps.putMapping(readFromfile("mapping.json"))
            } else {
                logger.info("klagebehandling does exist in Elasticsearch")
            }
        } catch (e: Exception) {
            logger.error("Unable to initialize Elasticsearch", e)
        }
    }

    private fun readFromfile(filename: String): Document {
        val text: String =
            ClassPathResource("elasticsearch/${filename}").inputStream.bufferedReader(Charsets.UTF_8).readText()
        return Document.parse(text)
    }

    fun save(klagebehandlinger: List<EsKlagebehandling>) {
        esTemplate.save(klagebehandlinger)
    }

    fun save(klagebehandling: EsKlagebehandling) {
        esTemplate.save(klagebehandling)
    }

    open fun findByCriteria(criteria: OppgaverSearchCriteria): SearchHits<EsKlagebehandling> {
        val query: Query = NativeSearchQueryBuilder()
            .withPageable(toPageable(criteria))
            .withSort(SortBuilders.fieldSort(sortField(criteria)).order(mapOrder(criteria.order)))
            .withQuery(criteria.toEsQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        println("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun countByCriteria(criteria: OppgaverSearchCriteria): Int {
        val query = NativeSearchQueryBuilder()
            .withQuery(criteria.toEsQuery())
            .build()
        return esTemplate.count(query, IndexCoordinates.of("klagebehandling")).toInt()
    }

    private fun sortField(criteria: OppgaverSearchCriteria): String =
        if (criteria.sortField == OppgaverSearchCriteria.SortField.MOTTATT) {
            "mottattKlageinstans"
        } else {
            "frist"
        }

    private fun mapOrder(order: OppgaverSearchCriteria.Order?): SortOrder {
        return order.let {
            when (it) {
                null -> SortOrder.ASC
                OppgaverSearchCriteria.Order.ASC -> SortOrder.ASC
                OppgaverSearchCriteria.Order.DESC -> SortOrder.DESC
            }
        }
    }

    private fun toPageable(criteria: OppgaverSearchCriteria): Pageable {
        val page: Int = (criteria.offset / criteria.limit)
        val size: Int = criteria.limit
        return PageRequest.of(page, size)
    }

    private fun OppgaverSearchCriteria.toEsQuery(): QueryBuilder {

        val baseQuery: BoolQueryBuilder = QueryBuilders.boolQuery()
        logger.debug("Search criteria: {}", this)

        val filterQuery = QueryBuilders.boolQuery()
        //TODO: Kunne nok vurdert å bruke filters også til andre ting enn sikkerhet, ref https://stackoverflow.com/questions/14595988/queries-vs-filters
        baseQuery.filter(filterQuery)
        if (!innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt()) {
            filterQuery.mustNot(QueryBuilders.termQuery("egenAnsatt", true))
        }
        if (!innloggetSaksbehandlerRepository.kanBehandleFortrolig()) {
            filterQuery.mustNot(QueryBuilders.termQuery("fortrolig", true))
        }
        if (!innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig()) {
            filterQuery.mustNot(QueryBuilders.termQuery("strengtFortrolig", true))
        }

        if (statuskategori == OppgaverSearchCriteria.Statuskategori.AAPEN) {
            baseQuery.mustNot(QueryBuilders.existsQuery("avsluttet"))
        } else {
            baseQuery.must(QueryBuilders.existsQuery("avsluttet"))
        }

        enhetsnr?.let {
            baseQuery.must(QueryBuilders.termQuery("tildeltEnhet", enhetsnr))
        }

        val innerQueryBehandlingtype = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryBehandlingtype)
        if (typer.isNotEmpty()) {
            typer.forEach {
                innerQueryBehandlingtype.should(QueryBuilders.termQuery("sakstype", it.name))
            }
        } else {
            innerQueryBehandlingtype.should(QueryBuilders.termQuery("sakstype", Sakstype.KLAGE.name))
        }

        val innerQueryTema = QueryBuilders.boolQuery()
        baseQuery.must(innerQueryTema)
        temaer.forEach {
            innerQueryTema.should(QueryBuilders.termQuery("tema", it.name))
        }

        erTildeltSaksbehandler?.let {
            if (erTildeltSaksbehandler) {
                baseQuery.must(QueryBuilders.existsQuery("tildeltSaksbehandlerident"))
            } else {
                baseQuery.mustNot(QueryBuilders.existsQuery("tildeltSaksbehandlerident"))
            }
        }
        saksbehandler?.let {
            baseQuery.must(QueryBuilders.termQuery("tildeltSaksbehandlerident", saksbehandler))
        }

        opprettetFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("mottattKlageinstans").gte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        opprettetTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("mottattKlageinstans").lte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        ferdigstiltFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("avsluttet").gte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        ferdigstiltTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("avsluttet").lte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        fristFom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("frist").gte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }
        fristTom?.let {
            baseQuery.must(
                QueryBuilders.rangeQuery("frist").lte(DateTimeFormatter.ISO_LOCAL_DATE.format(it))
            )
        }

        if (hjemler.isNotEmpty()) {
            val innerQueryHjemler = QueryBuilders.boolQuery()
            baseQuery.must(innerQueryHjemler)
            hjemler.forEach {
                innerQueryHjemler.should(QueryBuilders.termQuery("hjemler", it))
            }
        }

        logger.info("Making search request with query {}", baseQuery.toString())
        return baseQuery
    }

    fun deleteAll() {
        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        esTemplate.delete(query, EsKlagebehandling::class.java)
    }
}