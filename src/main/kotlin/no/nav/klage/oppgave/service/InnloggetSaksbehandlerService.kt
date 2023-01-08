package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.TokenUtil
import org.springframework.stereotype.Service

@Service
class InnloggetSaksbehandlerService(
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val tokenUtil: TokenUtil,
    private val saksbehandlerService: SaksbehandlerService
) {

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isKabalAdmin(): Boolean = saksbehandlerRepository.hasKabalAdminRole(tokenUtil.getIdent())

    fun kanBehandleFortrolig(): Boolean = saksbehandlerRepository.hasFortroligRole(tokenUtil.getIdent())

    fun kanBehandleStrengtFortrolig(): Boolean =
        saksbehandlerRepository.hasStrengtFortroligRole(tokenUtil.getIdent())

    fun kanBehandleEgenAnsatt(): Boolean =
        saksbehandlerRepository.hasEgenAnsattRole(tokenUtil.getIdent())

    fun harTilgangTilYtelse(ytelse: Ytelse): Boolean {
        return saksbehandlerService.saksbehandlerHasAccessToYtelse(getInnloggetIdent(), ytelse)
    }
}
