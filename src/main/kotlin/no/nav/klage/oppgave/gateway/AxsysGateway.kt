package no.nav.klage.oppgave.gateway

import no.nav.klage.oppgave.domain.saksbehandler.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.saksbehandler.SaksbehandlerIdent

interface AxsysGateway {

    fun getEnheterMedTemaerForSaksbehandler(ident: String): EnheterMedLovligeTemaer
    fun getSaksbehandlereIEnhet(enhetId: String): List<SaksbehandlerIdent>
}