package no.nav.klage.oppgave.config

import no.nav.klage.oppgave.repositories.ElasticsearchRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.elasticsearch.core.ElasticsearchOperations

@Configuration
@Import(ElasticsearchConfiguration::class)
class ElasticsearchServiceConfiguration {

    @Bean
    fun elasticsearchRepository(
        @Qualifier("elasticsearchOperations") elasticsearchTemplate: ElasticsearchOperations,
        innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository
    ): ElasticsearchRepository {
        return ElasticsearchRepository(elasticsearchTemplate, innloggetSaksbehandlerRepository)
    }

}