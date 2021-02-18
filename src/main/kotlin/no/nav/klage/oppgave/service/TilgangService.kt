package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.clients.egenansatt.EgenAnsattService
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.AuditLogEvent
import no.nav.klage.oppgave.domain.AuditLogEvent.Decision.ALLOW
import no.nav.klage.oppgave.domain.AuditLogEvent.Decision.DENY
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.util.AuditLogger
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class TilgangService(
    private val pdlFacade: PdlFacade,
    private val egenAnsattService: EgenAnsattService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val auditLogger: AuditLogger
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val securelogger = getSecureLogger()
    }

    private fun auditLog(fnr: String, decision: AuditLogEvent.Decision) {
        auditLogger.log(
            AuditLogEvent(
                navIdent = innloggetSaksbehandlerRepository.getInnloggetIdent(),
                action = AuditLogEvent.Action.KLAGEBEHANDLING_VIEW,
                decision = decision,
                personFnr = fnr
            )
        )
    }
    
    fun harSaksbehandlerTilgangTil(fnr: String): Boolean {
        val personInfo = pdlFacade.getPersonInfo(fnr)
        if (personInfo == null) {
            //TODO: Hva defaulter vi til? Velger å ikke deny nå, så får vi se..
            securelogger.error("PDL didnt return anything for fnr $fnr, we default to give access")
        } else {
            if (personInfo.harBeskyttelsesbehovFortrolig()) {
                securelogger.info("erFortrolig")
                if (innloggetSaksbehandlerRepository.kanBehandleFortrolig()) {
                    securelogger.info("Access granted to fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                } else {
                    securelogger.info("Access denied to fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                    auditLog(fnr, DENY)
                    return false
                }
            }
            if (personInfo.harBeskyttelsesbehovStrengtFortrolig()) {
                securelogger.info("erStrengtFortrolig")
                if (innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig()) {
                    securelogger.info("Access granted to strengt fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                } else {
                    securelogger.info("Access denied to strengt fortrolig for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                    auditLog(fnr, DENY)
                    return false
                }
            }
        }
        if (egenAnsattService.erEgenAnsatt(fnr)) {
            securelogger.info("erEgenAnsatt")
            if (innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt()) {
                securelogger.info("Access granted to egen ansatt for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
            } else {
                securelogger.info("Access denied to egen ansatt for ${innloggetSaksbehandlerRepository.getInnloggetIdent()}")
                auditLog(fnr, DENY)
                return false
            }
        }
        auditLog(fnr, ALLOW)
        return true
    }
}