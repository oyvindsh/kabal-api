package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.repositories.EsKlagebehandlingRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.ElasticsearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Configuration
@EnableElasticsearchRepositories(basePackageClasses = [EsKlagebehandlingRepository::class])
class ElasticsearchServiceConfiguration {

    @Bean
    fun elasticsearchService(
        elasticsearchRestTemplate: ElasticsearchRestTemplate,
        innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
        esKlagebehandlingRepository: EsKlagebehandlingRepository
    ): ElasticsearchService {
        return ElasticsearchService(
            elasticsearchRestTemplate,
            innloggetSaksbehandlerRepository,
            esKlagebehandlingRepository
        )
    }

}