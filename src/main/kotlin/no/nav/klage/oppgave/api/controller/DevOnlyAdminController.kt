package no.nav.klage.oppgave.api.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.finn.unleash.Unleash
import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.service.VedtakKafkaProducer
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
    private val vedtakKafkaProducer: VedtakKafkaProducer,
    private val unleash: Unleash
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
    fun deleteBehandling(@PathVariable("id") behandlingId: UUID,) {
        try {
            logger.info("Delete behandling i dev")
            adminService.deleteBehandlingInDev(behandlingId)
        } catch (e: Exception) {
            logger.warn("Failed to delete behandling i dev")
            throw e
        }
    }

    @Unprotected
    @PostMapping("/internal/produce_kafka_message")
    @ResponseStatus(HttpStatus.CREATED)
    fun putResultOnKafkaTopicForTestingPurposes(
        @RequestBody json: String
    ) {

        val klagevedtakFattet = jacksonObjectMapper().readValue(json, KlagevedtakFattet::class.java)

//        KlagevedtakFattet(
//            kildeReferanse = "deres_ref",
//            kilde = "K9",
//            utfall = Utfall.MEDHOLD,
//            vedtaksbrevReferanse = "journalpost_id_her",
//            kabalReferanse = "unik_ref_til_kabal"
//        )

        vedtakKafkaProducer.sendVedtak(klagevedtakFattet)
    }
}