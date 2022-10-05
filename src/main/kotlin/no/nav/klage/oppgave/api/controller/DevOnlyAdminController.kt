package no.nav.klage.oppgave.api.controller

import no.finn.unleash.Unleash
import no.nav.klage.oppgave.api.view.BehandlingDetaljerView
import no.nav.klage.oppgave.api.view.SearchFullmektigInput
import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.service.FullmektigSearchService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@Profile("dev-gcp")
@RestController
class DevOnlyAdminController(
    private val adminService: AdminService,
    private val unleash: Unleash,
    private val fullmektigSearchService: FullmektigSearchService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Unprotected
    @GetMapping("/internal/kafkaadmin/refill", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun resetElasticIndex() {
        try {
            logger.info("Syncing db with Kafka")
            adminService.syncKafkaWithDb()
        } catch (e: Exception) {
            logger.warn("Failed to resync db with Kafka")
            throw e
        }
    }

    @Unprotected
    @DeleteMapping("/internal/behandlinger/{id}", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun deleteBehandling(@PathVariable("id") behandlingId: UUID) {
        try {
            logger.info("Delete behandling i dev")
            adminService.deleteBehandlingInDev(behandlingId)
        } catch (e: Exception) {
            logger.warn("Failed to delete behandling i dev")
            throw e
        }
    }

    @Unprotected
    @PostMapping("/internal/generatemissingankeitrygderettendev", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun generateMissingAnkeITrygderetten() {
        logger.debug("generateMissingAnkeITrygderetten is called in dev")

        try {
            adminService.generateMissingAnkeITrygderetten()
        } catch (e: Exception) {
            logger.warn("Failed to generate missing AnkeITrygderetten", e)
            throw e
        }
    }

    @Unprotected
    @PostMapping("/internal/searchfullmektig")
    fun searchFullmektig(
        @RequestBody input: SearchFullmektigInput,
    ): BehandlingDetaljerView.ProsessfullmektigView {
        return fullmektigSearchService.searchFullmektig(identifikator = input.identifikator, skipAccessControl = true)
    }
}