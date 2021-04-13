package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.mapper.KlagebehandlingMapper
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service

@Service
class IndexService(
    private val elasticsearchRepository: ElasticsearchRepository,
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val klagebehandlingMapper: KlagebehandlingMapper
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    fun deleteAllKlagebehandlinger() {
        elasticsearchRepository.deleteAll()
    }

    fun reindexAllKlagebehandlinger() {
        var pageable: Pageable =
            PageRequest.of(0, 50, Sort.by("created").descending())
        do {
            val page = klagebehandlingRepository.findAll(pageable)
            page.content.map { klagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(it) }
                .let {
                    try {
                        elasticsearchRepository.save(it)
                    } catch (e: Exception) {
                        logger.warn("Exception during indexing", e)
                    }
                }
            pageable = page.nextPageable();
        } while (pageable.isPaged)
    }

    fun findAndLogOutOfSyncKlagebehandlinger() {
        val idsInEs = elasticsearchRepository.findAllIds()
        val idsInDb = idsInDb()
        logger.info("Number of klagebehandlinger in ES: ${idsInEs.size}, number of klagebehandlinger in DB: ${idsInDb.size}")
        logger.info("Klagebehandlinger in ES that are not in DB: {}", idsInEs.minus(idsInDb))
        logger.info("Klagebehandlinger in DB that are not in ES: {}", idsInDb.minus(idsInEs))
        //TODO: Are they up to date?
    }

    private fun idsInDb(): List<String> {
        val idsInDb = mutableListOf<String>()
        var pageable: Pageable =
            PageRequest.of(0, 50)
        do {
            val page = klagebehandlingRepository.findAll(pageable)
            page.content.map { it.id.toString() }.let { idsInDb.addAll(it) }
            pageable = page.nextPageable();
        } while (pageable.isPaged)
        return idsInDb
    }


    @Retryable
    fun indexKlagebehandling(klagebehandling: Klagebehandling) {
        try {
            elasticsearchRepository.save(
                klagebehandlingMapper.mapKlagebehandlingOgMottakToEsKlagebehandling(klagebehandling)
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

}