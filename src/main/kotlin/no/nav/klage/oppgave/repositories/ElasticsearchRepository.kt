package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.elasticsearch.KlageStatistikk
import no.nav.klage.oppgave.domain.elasticsearch.RelatedKlagebehandlinger
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.util.getLogger
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.range.ParsedDateRange
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
                val mapping: MutableMap<String, Any> = indexOps.mapping
                logger.debug("Current index has mapping $mapping")
                mapping.forEach { (key, value) -> logger.debug("mapping key: $key, value: $value") }
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
        refresh()
    }

    fun save(klagebehandling: EsKlagebehandling) {
        esTemplate.save(klagebehandling)
        refresh()
    }

    open fun findByCriteria(criteria: KlagebehandlingerSearchCriteria): SearchHits<EsKlagebehandling> {
        val query: Query = NativeSearchQueryBuilder()
            .withPageable(toPageable(criteria))
            .withSort(SortBuilders.fieldSort(sortField(criteria)).order(mapOrder(criteria.order)))
            .withQuery(criteria.toEsQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        println("ANTALL TREFF: ${searchHits.totalHits}")
        return searchHits
    }

    open fun countByCriteria(criteria: KlagebehandlingerSearchCriteria): Int {
        val query = NativeSearchQueryBuilder()
            .withQuery(criteria.toEsQuery())
            .build()
        return esTemplate.count(query, IndexCoordinates.of("klagebehandling")).toInt()
    }

    private fun sortField(criteria: KlagebehandlingerSearchCriteria): String =
        if (criteria.sortField == KlagebehandlingerSearchCriteria.SortField.MOTTATT) {
            "mottattKlageinstans"
        } else {
            "frist"
        }

    private fun mapOrder(order: KlagebehandlingerSearchCriteria.Order?): SortOrder {
        return order.let {
            when (it) {
                null -> SortOrder.ASC
                KlagebehandlingerSearchCriteria.Order.ASC -> SortOrder.ASC
                KlagebehandlingerSearchCriteria.Order.DESC -> SortOrder.DESC
            }
        }
    }

    private fun toPageable(criteria: KlagebehandlingerSearchCriteria): Pageable {
        val page: Int = (criteria.offset / criteria.limit)
        val size: Int = criteria.limit
        return PageRequest.of(page, size)
    }

    private fun KlagebehandlingerSearchCriteria.toEsQuery(): QueryBuilder {

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

        if (statuskategori == KlagebehandlingerSearchCriteria.Statuskategori.AAPEN) {
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

    fun refresh() {
        esTemplate.indexOps(IndexCoordinates.of("klagebehandling")).refresh()
    }

    fun deleteAll() {
        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        esTemplate.delete(query, EsKlagebehandling::class.java)
        refresh()
    }

    fun findAllIds(): List<String> {
        val allQuery: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(allQuery, EsKlagebehandling::class.java)
        return searchHits.searchHits.map { it.id!! }
    }

    open fun findRelatedKlagebehandlinger(
        fnr: String,
        saksreferanse: String,
        journalpostIder: List<String>
    ): RelatedKlagebehandlinger {
        val aapneByFnr = klagebehandlingerMedFoedselsnummer(fnr, true)
        val aapneBySaksreferanse = klagebehandlingerMedSaksreferanse(saksreferanse, true)
        val aapneByJournalpostid = klagebehandlingerMedJournalpostId(journalpostIder, true)
        val avsluttedeByFnr = klagebehandlingerMedFoedselsnummer(fnr, false)
        val avsluttedeBySaksreferanse = klagebehandlingerMedSaksreferanse(saksreferanse, false)
        val avsluttedeByJournalpostid = klagebehandlingerMedJournalpostId(journalpostIder, false)
        //TODO: Vi trenger vel neppe returnere hele klagebehandlingen.. Hva trenger vi å vise i gui?
        return RelatedKlagebehandlinger(
            aapneByFnr,
            avsluttedeByFnr,
            aapneBySaksreferanse,
            avsluttedeBySaksreferanse,
            aapneByJournalpostid,
            avsluttedeByJournalpostid
        )
    }

    private fun klagebehandlingerMedFoedselsnummer(fnr: String, aapen: Boolean): List<EsKlagebehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termQuery("foedselsnummer", fnr)), aapen
        )
    }

    private fun klagebehandlingerMedSaksreferanse(saksreferanse: String, aapen: Boolean): List<EsKlagebehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termQuery("saksreferanse", saksreferanse)), aapen
        )
    }

    private fun klagebehandlingerMedJournalpostId(
        journalpostIder: List<String>,
        aapen: Boolean
    ): List<EsKlagebehandling> {
        return findWithBaseQueryAndAapen(
            QueryBuilders.boolQuery().must(QueryBuilders.termsQuery("journalpostId", journalpostIder)), aapen
        )
    }

    private fun findWithBaseQueryAndAapen(baseQuery: BoolQueryBuilder, aapen: Boolean): List<EsKlagebehandling> {
        if (aapen) {
            baseQuery.mustNot(QueryBuilders.existsQuery("avsluttet"))
        } else {
            baseQuery.must(QueryBuilders.existsQuery("avsluttet"))
        }
        return try {
            esTemplate.search(
                NativeSearchQueryBuilder().withQuery(baseQuery).build(),
                EsKlagebehandling::class.java
            ).searchHits.map { it.content }
        } catch (e: Exception) {
            logger.error("Failed to search ES for related klagebehandlinger", e)
            emptyList()
        }
    }

    open fun statistikkQuery(): KlageStatistikk {

        val baseQueryInnsendtOgAvsluttet: QueryBuilder = QueryBuilders.matchAllQuery()
        val queryBuilderInnsendtOgAvsluttet: NativeSearchQueryBuilder = NativeSearchQueryBuilder()
            .withQuery(baseQueryInnsendtOgAvsluttet)
        addAggregationsForInnsendtAndAvsluttet(queryBuilderInnsendtOgAvsluttet)

        val searchHitsInnsendtOgAvsluttet: SearchHits<EsKlagebehandling> =
            esTemplate.search(queryBuilderInnsendtOgAvsluttet.build(), EsKlagebehandling::class.java)

        val innsendtOgAvsluttetAggs = searchHitsInnsendtOgAvsluttet.aggregations!!
        val sumInnsendtYesterday =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("innsendt_yesterday").buckets.firstOrNull()?.docCount ?: 0
        val sumInnsendtLastSevenDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("innsendt_last7days").buckets.firstOrNull()?.docCount ?: 0
        val sumInnsendtLastThirtyDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("innsendt_last30days").buckets.firstOrNull()?.docCount ?: 0
        val sumAvsluttetYesterday =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("avsluttet_yesterday").buckets.firstOrNull()?.docCount ?: 0
        val sumAvsluttetLastSevenDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("avsluttet_last7days").buckets.firstOrNull()?.docCount ?: 0
        val sumAvsluttetLastThirtyDays =
            innsendtOgAvsluttetAggs.get<ParsedDateRange>("avsluttet_last30days").buckets.firstOrNull()?.docCount ?: 0

        val baseQueryOverFrist: BoolQueryBuilder = QueryBuilders.boolQuery()
        baseQueryOverFrist.mustNot(QueryBuilders.existsQuery("avsluttet"))
        val queryBuilderOverFrist: NativeSearchQueryBuilder = NativeSearchQueryBuilder()
            .withQuery(baseQueryOverFrist)
        addAggregationsForOverFrist(queryBuilderOverFrist)

        val searchHitsOverFrist: SearchHits<EsKlagebehandling> =
            esTemplate.search(queryBuilderOverFrist.build(), EsKlagebehandling::class.java)
        val sumOverFrist =
            searchHitsOverFrist.aggregations!!.get<ParsedDateRange>("over_frist").buckets.firstOrNull()?.docCount ?: 0
        val sumUbehandlede = searchHitsOverFrist.totalHits
        return KlageStatistikk(
            sumUbehandlede,
            sumOverFrist,
            sumInnsendtYesterday,
            sumInnsendtLastSevenDays,
            sumInnsendtLastThirtyDays,
            sumAvsluttetYesterday,
            sumAvsluttetLastSevenDays,
            sumAvsluttetLastThirtyDays
        )
    }

    private fun addAggregationsForOverFrist(querybuilder: NativeSearchQueryBuilder) {
        querybuilder
            .addAggregation(
                AggregationBuilders.dateRange("over_frist").field("frist").addUnboundedTo("now/d").format("yyyy-MM-dd")
            )
    }

    private fun addAggregationsForInnsendtAndAvsluttet(querybuilder: NativeSearchQueryBuilder) {
        querybuilder
            .addAggregation(
                AggregationBuilders.dateRange("innsendt_yesterday").field("innsendt").addRange("now-1d/d", "now/d")
                    .format("yyyy-MM-dd")
            )
            .addAggregation(
                AggregationBuilders.dateRange("innsendt_last7days").field("innsendt").addRange("now-7d/d", "now/d")
                    .format("yyyy-MM-dd")
            )
            .addAggregation(
                AggregationBuilders.dateRange("innsendt_last30days").field("innsendt").addRange("now-30d/d", "now/d")
                    .format("yyyy-MM-dd")
            )
            .addAggregation(
                AggregationBuilders.dateRange("avsluttet_yesterday").field("avsluttet").addRange("now-1d/d", "now/d")
                    .format("yyyy-MM-dd")
            )
            .addAggregation(
                AggregationBuilders.dateRange("avsluttet_last7days").field("avsluttet").addRange("now-7d/d", "now/d")
                    .format("yyyy-MM-dd")
            )
            .addAggregation(
                AggregationBuilders.dateRange("avsluttet_last30days").field("avsluttet").addRange("now-30d/d", "now/d")
                    .format("yyyy-MM-dd")
            )
    }
}