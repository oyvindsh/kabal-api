package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.exceptions.MissingTilgangException
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController

@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class AdminController(
    private val adminService: AdminService,
    private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository,
    private val eregClient: EregClient,
    private val azureGateway: AzureGateway
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/internal/elasticadmin/rebuild", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun resetElasticIndexWithPost() {


        azureGateway.getDataOmInnloggetSaksbehandler()
        azureGateway.getRollerForInnloggetSaksbehandler()

        krevAdminTilgang()
        try {
            adminService.recreateEsIndex()
            adminService.syncEsWithDb()
            adminService.findAndLogOutOfSyncKlagebehandlinger()
        } catch (e: Exception) {
            logger.warn("Failed to reset ES index", e)
            throw e
        }
    }

    @PostMapping("/internal/dvh/resend", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun resendStatsToDVH() {
        logger.debug("resendStatsToDVH is called")
        krevAdminTilgang()

        try {
            adminService.resendToDVH()
        } catch (e: Exception) {
            logger.warn("Failed to resend to DVH", e)
            throw e
        }
    }

    private fun krevAdminTilgang() {
        if (!innloggetSaksbehandlerRepository.erAdmin()) {
            throw MissingTilgangException("Not an admin")
        }
    }

    @Unprotected
    @GetMapping("/internal/testazure", produces = ["application/json"])
    fun testAzure(): String {
        logger.debug("" + azureGateway.getPersonligDataOmSaksbehandlerMedIdent("Z994488"))
        logger.debug("" + azureGateway.getAllDisplayNames(listOf(listOf("Z994488"))))
        logger.debug("" + azureGateway.getRollerForSaksbehandlerMedIdent("Z994488"))
        logger.debug("" + azureGateway.getRolleIder("Z994488"))
        logger.debug("" + azureGateway.getGroupMembersNavIdents("07add1e7-7195-4c37-828d-fdf23ec6bef1"))
        return "ok"
    }

    @Unprotected
    @GetMapping("/internal/testereg", produces = ["application/json"])
    fun testEreg(): String {

        try {
            logger.info("1: ${eregClient.hentOrganisasjon("912733300")?.navn}")
            logger.info("1: ${eregClient.hentOrganisasjon("990888213")?.navn}")
            logger.info("1: ${eregClient.hentOrganisasjon("923609016")?.navn}")
            logger.info("1: ${eregClient.hentOrganisasjon("973861883")?.navn}")
        } catch (e: Exception) {
            logger.warn("Failed to lookup org in ereg", e)
            throw e
        }

        return "ok"
    }

}