package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate

@Configuration
class ElasticsearchServiceConfiguration {

    @Bean
    fun elasticsearchRepository(
        elasticsearchRestTemplate: ElasticsearchRestTemplate,
        innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
    ): ElasticsearchRepository {
        return ElasticsearchRepository(elasticsearchRestTemplate, innloggetSaksbehandlerRepository)
    }

}