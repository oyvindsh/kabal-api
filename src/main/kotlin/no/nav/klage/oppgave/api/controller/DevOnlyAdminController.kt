package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.domain.kafka.KlagevedtakFattet
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import no.nav.klage.oppgave.service.AdminService
import no.nav.klage.oppgave.service.VedtakKafkaProducer
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Profile("dev-gcp")
@RestController
class DevOnlyAdminController(
    private val adminService: AdminService,
    private val vedtakKafkaProducer: VedtakKafkaProducer
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Unprotected
    @GetMapping("/internal/elasticadmin/nuke", produces = ["application/json"])
    @ResponseStatus(HttpStatus.OK)
    fun resetElasticIndex() {
        try {
            adminService.recreateEsIndex()
            adminService.syncEsWithDb()
            adminService.findAndLogOutOfSyncKlagebehandlinger()
        } catch (e: Exception) {
            logger.warn("Failed to reset ES index", e)
            throw e
        }
    }

    @Unprotected
    @PostMapping("/internal/produce_kafka_message")
    @ResponseStatus(HttpStatus.CREATED)
    fun putResultOnKafkaTopicForTestingPurposes() {

        val klagevedtakFattet = KlagevedtakFattet(
            kildeReferanse = "deres_ref",
            kilde = "K9",
            utfall = Utfall.MEDHOLD,
            vedtaksbrevReferanse = "journalpost_id_her",
            kabalReferanse = "unik_ref_til_kabal"
        )

        vedtakKafkaProducer.sendVedtak(klagevedtakFattet)
    }
}