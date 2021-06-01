package no.nav.klage.oppgave.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.binder.MeterBinder
import no.nav.klage.oppgave.service.ElasticsearchService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class FunksjonelleGaugesConfiguration {

    @Bean
    fun registerFunctionalStats(elasticsearchService: ElasticsearchService): MeterBinder {
        return MeterBinder { registry: MeterRegistry ->
            Gauge.builder("funksjonell.ikketildelt") { elasticsearchService.countIkkeTildelt() }.register(registry)
            Gauge.builder("funksjonell.tildelt") { elasticsearchService.countTildelt() }.register(registry)
            Gauge.builder("funksjonell.sendttilmedunderskriver") { elasticsearchService.countSendtTilMedunderskriver() }
                .register(registry)
            Gauge.builder("funksjonell.avsluttetavsaksbehandler") { elasticsearchService.countAvsluttetAvMedunderskriver() }
                .register(registry)
            Gauge.builder("funksjonell.avsluttet") { elasticsearchService.countAvsluttet() }.register(registry)
            Gauge.builder("funksjonell.antallsaksdokumenterpaaavsluttedebehandlinger.median") { elasticsearchService.countAntallSaksdokumenterIAvsluttedeBehandlingerMedian() }
                .register(registry)
            //TODO: Egentlig ønsker jeg å registrere antall saksdokumenter per klagebehandling, med klagebehandlingId'en som en tag i gaugen. Men hvordan i all verden gjør jeg det??
        }
    }

}