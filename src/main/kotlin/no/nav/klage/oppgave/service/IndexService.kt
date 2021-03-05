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
            page.content.forEach { indexKlagebehandling(it) }
            pageable = page.nextPageable();
        } while (pageable.isPaged)
    }

    fun findAndLogOldKlagebehandlinger(): Pair<Long, Long> =
        elasticsearchRepository.findAndLogKlagebehandlingerNotUpdated()


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