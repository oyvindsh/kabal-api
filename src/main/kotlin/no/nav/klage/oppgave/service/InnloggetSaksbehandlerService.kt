package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.TokenUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerService(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tokenUtil: TokenUtil,
    @Value("\${KABAL_SAKSBEHANDLING}") private val kabalSaksbehandling: String,
    @Value("\${KABAL_FAGTEKSTREDIGERING}") private val kabalFagtekstredigering: String,
    @Value("\${KABAL_OPPGAVESTYRING_EGEN_ENHET}") private val kabalOppgavestyringEgenEnhet: String,
    @Value("\${FORTROLIG}") private val fortrolig: String,
    @Value("\${STRENGT_FORTROLIG}") private val strengtFortrolig: String,
    @Value("\${EGEN_ANSATT}") private val egenAnsatt: String,
    @Value("\${KABAL_ADMIN}") private val kabalAdmin: String
) {

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isKabalAdmin(): Boolean = tokenUtil.getRollerFromToken().hasRole(kabalAdmin)

    fun kanBehandleFortrolig(): Boolean = tokenUtil.getRollerFromToken().hasRole(fortrolig)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenUtil.getRollerFromToken().hasRole(strengtFortrolig)

    fun kanBehandleEgenAnsatt(): Boolean = tokenUtil.getRollerFromToken().hasRole(egenAnsatt)

    fun harTilgangTilYtelse(ytelse: Ytelse): Boolean {
        return saksbehandlerRepository.harTilgangTilYtelse(getInnloggetIdent(), ytelse)
    }

    private fun List<String>.hasRole(role: String) = any { it.contains(role) }
}
