package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

interface EsKlagebehandlingRepository : ElasticsearchRepository<EsKlagebehandling, String>