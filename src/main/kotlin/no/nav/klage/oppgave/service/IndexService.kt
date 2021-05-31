package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.mapper.EsKlagebehandlingMapper
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class IndexService(
    private val elasticsearchService: ElasticsearchService,
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val esKlagebehandlingMapper: EsKlagebehandlingMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun deleteAllKlagebehandlinger() {
        elasticsearchService.deleteAll()
    }

    fun reindexAllKlagebehandlinger() {
        var pageable: Pageable =
            PageRequest.of(0, 50, Sort.by("created").descending())
        do {
            val page = klagebehandlingRepository.findAll(pageable)
            page.content.map { esKlagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(it) }
                .let {
                    try {
                        elasticsearchService.save(it)
                    } catch (e: Exception) {
                        logger.warn("Exception during indexing", e)
                    }
                }
            pageable = page.nextPageable();
        } while (pageable.isPaged)
    }

    fun findAndLogOutOfSyncKlagebehandlinger() {
        val esData = elasticsearchService.findAllIdAndModified()
        val dbData = idAndModifiedInDb()
        logger.info("Number of klagebehandlinger in ES: ${esData.size}, number of klagebehandlinger in DB: ${dbData.size}")
        logger.info(
            "Klagebehandlinger in ES that are not in DB: {}",
            esData.keys.minus(dbData.keys)
        )
        logger.info(
            "Klagebehandlinger in DB that are not in ES: {}",
            dbData.keys.minus(esData.keys)
        )
        dbData.keys.forEach {
            if (!dbData.getValue(it).isEqual(esData[it])) {
                logger.info(
                    "Klagebehandling {} is not up-to-date in ES, modified is {} in DB and {} in ES",
                    it,
                    dbData.getValue(it),
                    esData[it]
                )
            }
        }
    }

    private fun idAndModifiedInDb(): Map<String, LocalDateTime> {
        val idsInDb = mutableListOf<Pair<String, LocalDateTime>>()
        var pageable: Pageable =
            PageRequest.of(0, 50)
        do {
            val page = klagebehandlingRepository.findAll(pageable)
            page.content.map { it.id.toString() to it.modified }.let { idsInDb.addAll(it) }
            pageable = page.nextPageable();
        } while (pageable.isPaged)
        return idsInDb.toMap()
    }


    @Retryable
    fun indexKlagebehandling(klagebehandling: Klagebehandling) {
        try {
            elasticsearchService.save(
                esKlagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling)
            )
        } catch (e: Exception) {
            if (e.message?.contains("version_conflict_engine_exception") == true) {
                logger.info("Later version already indexed, ignoring this..")
            } else {
                logger.error("Unable to index klagebehandling ${klagebehandling.id}, see securelogs for details")
                securelogger.error("Unable to index klagebehandling ${klagebehandling.id}", e)
            }
        }
    }

    fun recreateIndex() {
        elasticsearchService.recreateIndex()
    }

}