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
    @Value("\${FORTROLIG_ROLE_ID}") private val fortroligRoleId: String,
    @Value("\${STRENGT_FORTROLIG_ROLE_ID}") private val strengtFortroligRoleId: String,
    @Value("\${EGEN_ANSATT_ROLE_ID}") private val egenAnsattRoleId: String,
    @Value("\${KABAL_ADMIN_ROLE_ID}") private val kabalAdminRoleId: String
) {

    fun getInnloggetIdent() = tokenUtil.getIdent()

    fun isKabalAdmin(): Boolean = tokenUtil.getRoleIdsFromToken().contains(kabalAdminRoleId)

    fun kanBehandleFortrolig(): Boolean = tokenUtil.getRoleIdsFromToken().contains(fortroligRoleId)

    fun kanBehandleStrengtFortrolig(): Boolean =
        tokenUtil.getRoleIdsFromToken().contains(strengtFortroligRoleId)

    fun kanBehandleEgenAnsatt(): Boolean = tokenUtil.getRoleIdsFromToken().contains(egenAnsattRoleId)

    fun harTilgangTilYtelse(ytelse: Ytelse): Boolean {
        return saksbehandlerRepository.harTilgangTilYtelse(getInnloggetIdent(), ytelse)
    }
}
