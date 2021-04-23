package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AdminController(private val adminService: AdminService) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Unprotected
    @GetMapping("/internal/elasticadmin/nuke", produces = ["application/json"])
    fun resetElasticIndex(): ElasticAdminResponse {
        //TODO: Trenger auth så ikke hvem som helst kan kalle denne
        try {
            adminService.recreateEsIndex()
            adminService.syncEsWithDb()
            adminService.findAndLogOutOfSyncKlagebehandlinger()
        } catch (e: Exception) {
            logger.warn("Failed to reset ES index", e)
            throw e
        }

        return ElasticAdminResponse("ok")
    }
}

data class ElasticAdminResponse(private val status: String)