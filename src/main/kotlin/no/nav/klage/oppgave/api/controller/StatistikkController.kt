package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.domain.elasticsearch.KlageStatistikk
import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatistikkController(private val elasticsearchRepository: ElasticsearchRepository) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Unprotected
    @GetMapping("/statistikk/klagebehandlinger", produces = ["application/json"])
    fun resetElasticIndex(): KlageStatistikk {
        return elasticsearchRepository.statistikkQuery()
    }
}
