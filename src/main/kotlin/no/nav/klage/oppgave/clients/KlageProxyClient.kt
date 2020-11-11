package no.nav.klage.oppgave.clients

import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component

@Component
class KlageProxyClient {

    @Retryable
    //TODO: Ta i bruk @Cacheable
    fun getRoller(ident: String): List<String> {
        return emptyList()
        //TODO: Kalle klage-fss-proxy sin /roller, men den må utvides så den støtter mer enn bare innlogget bruker
    }

}
