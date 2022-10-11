package no.nav.klage.oppgave.service.distribusjon

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.clients.kaka.KakaApiGateway
import no.nav.klage.oppgave.domain.kafka.EventType
import no.nav.klage.oppgave.domain.kafka.UtsendingStatus.FEILET
import no.nav.klage.oppgave.domain.kafka.UtsendingStatus.IKKE_SENDT
import no.nav.klage.oppgave.service.BehandlingService
import no.nav.klage.oppgave.service.KafkaDispatcher
import no.nav.klage.oppgave.util.getLogger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class KlagebehandlingSchedulerService(
    private val behandlingService: BehandlingService,
    private val behandlingAvslutningService: BehandlingAvslutningService,
    private val kafkaDispatcher: KafkaDispatcher,
    private val kakaApiGateway: KakaApiGateway
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Scheduled(fixedDelay = 10000, initialDelay = 20000)
    @SchedulerLock(name = "avsluttBehandling")
    fun avsluttBehandling() {
        logger.debug("avsluttBehandling is called by scheduler")
        //TODO: Vurder om ankebehandling basert på ankeITrygderetten skal opprettes raskere enn etter 4 minutter. Evt: Hvorfor vente 4 minutter?
        val behandlingIdList: List<Pair<UUID, Type>> = behandlingService.findBehandlingerForAvslutning()

        behandlingIdList.forEach { (id, type) ->
            if (type != Type.ANKE_I_TRYGDERETTEN) {
                kakaApiGateway.finalizeBehandling(
                    behandlingService.getBehandlingForReadWithoutCheckForAccess(
                        id
                    )
                )
            }
            behandlingAvslutningService.avsluttBehandling(id)
        }
    }

    @Scheduled(fixedDelay = 240000, initialDelay = 300000)
    @SchedulerLock(name = "dispatchUnsentVedtakToKafka")
    fun dispatchUnsentVedtakToKafka() {
        logger.debug("dispatchUnsentVedtakToKafka is called by scheduler")
        kafkaDispatcher.dispatchEventsToKafka(
            type = EventType.KLAGE_VEDTAK,
            utsendingStatusList = listOf(IKKE_SENDT, FEILET)
        )
    }

    @Scheduled(fixedDelay = 240000, initialDelay = 360000)
    @SchedulerLock(name = "dispatchUnsentDVHStatsToKafka")
    fun dispatchUnsentDVHStatsToKafka() {
        logger.debug("dispatchUnsentDVHStatsToKafka is called by scheduler")
        kafkaDispatcher.dispatchEventsToKafka(
            type = EventType.STATS_DVH,
            utsendingStatusList = listOf(IKKE_SENDT, FEILET)
        )
    }

    @Scheduled(fixedDelay = 240000, initialDelay = 420000)
    @SchedulerLock(name = "dispatchUnsentBehandlingEventsToKafka")
    fun dispatchUnsentBehandlingEventToKafka() {
        logger.debug("dispatchUnsentBehandlingEventToKafka is called by scheduler")
        kafkaDispatcher.dispatchEventsToKafka(
            type = EventType.BEHANDLING_EVENT,
            utsendingStatusList = listOf(IKKE_SENDT, FEILET)
        )
    }
}