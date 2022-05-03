package no.nav.klage.oppgave.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.api.view.KildeFagsystem
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

fun MeterRegistry.incrementMottattKlageAnke(kildesystem: KildeFagsystem, ytelse: Ytelse, type: Type) {
    counter(
        MetricsConfiguration.MOTTATT_KLAGEANKE,
        "kildesystem",
        kildesystem.name,
        "ytelse",
        ytelse.navn,
        "type",
        type.navn
    ).increment()
}