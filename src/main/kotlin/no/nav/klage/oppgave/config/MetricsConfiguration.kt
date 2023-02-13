package no.nav.klage.oppgave.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.annotation.Configuration


@Configuration
class MetricsConfiguration {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val MOTTATT_KLAGEANKE = "funksjonell.mottattklageanke"
    }
}

fun MeterRegistry.incrementMottattKlageAnke(kildesystem: String, ytelse: String, type: String) {
    counter(
        MetricsConfiguration.MOTTATT_KLAGEANKE,
        "kildesystem",
        kildesystem,
        "ytelse",
        ytelse,
        "type",
        type
    ).increment()
}