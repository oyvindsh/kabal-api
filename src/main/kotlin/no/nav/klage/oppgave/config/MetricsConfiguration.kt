package no.nav.klage.oppgave.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.config.MeterFilter
import io.micrometer.influx.InfluxMeterRegistry
import no.nav.klage.oppgave.util.getLogger
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class MetricsConfiguration {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val MOTTATT_KLAGE = "funksjonell.mottattklage"
    }

    @Bean
    fun influxMetricsCustomization(): MeterRegistryCustomizer<InfluxMeterRegistry>? {
        return MeterRegistryCustomizer<InfluxMeterRegistry> { registry ->
            registry.config().meterFilter(
                MeterFilter.denyUnless { it.name.startsWith("funksjonell") }
            )
        }
    }
}

fun MeterRegistry.incrementMottattKlage(kildesystem: String, ytelse: String) {
    counter(MetricsConfiguration.MOTTATT_KLAGE, "kildesystem", kildesystem, "ytelse", ytelse).increment()
}