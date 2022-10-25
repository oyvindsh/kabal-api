package no.nav.klage.oppgave.eventlisteners

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.hjemmel.Registreringshjemmel
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.kafka.*
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.repositories.KafkaEventRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional
class StatistikkTilDVHService(
    private val kafkaEventRepository: KafkaEventRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())
    }

    fun process(behandlingEndretEvent: BehandlingEndretEvent) {
        if (shouldSendStats(behandlingEndretEvent)) {
            val behandling = behandlingEndretEvent.behandling

            val eventId = UUID.randomUUID()
            val statistikkTilDVH = createStatistikkTilDVH(
                eventId = eventId,
                behandling = behandling,
                behandlingState = getBehandlingState(behandlingEndretEvent)
            )

            kafkaEventRepository.save(
                KafkaEvent(
                    id = eventId,
                    behandlingId = behandlingEndretEvent.behandling.id,
                    kilde = behandlingEndretEvent.behandling.sakFagsystem.navn,
                    kildeReferanse = behandlingEndretEvent.behandling.kildeReferanse,
                    status = UtsendingStatus.IKKE_SENDT,
                    jsonPayload = statistikkTilDVH.toJson(),
                    type = EventType.STATS_DVH
                )
            )
        }
    }

    private fun StatistikkTilDVH.toJson(): String = objectMapper.writeValueAsString(this)

    private fun shouldSendStats(behandlingEndretEvent: BehandlingEndretEvent) =
        (behandlingEndretEvent.endringslogginnslag.isEmpty() && behandlingEndretEvent.behandling.type != Type.ANKE_I_TRYGDERETTEN) ||
                behandlingEndretEvent.endringslogginnslag.any {
                    it.felt === Felt.TILDELT_SAKSBEHANDLERIDENT
                            || it.felt === Felt.AVSLUTTET_AV_SAKSBEHANDLER
                            || it.felt === Felt.KJENNELSE_MOTTATT
                }

    private fun getBehandlingState(behandlingEndretEvent: BehandlingEndretEvent): BehandlingState {
        val endringslogginnslag: List<Endringslogginnslag> = behandlingEndretEvent.endringslogginnslag
        return when {
            endringslogginnslag.isEmpty() && behandlingEndretEvent.behandling.type != Type.ANKE_I_TRYGDERETTEN -> BehandlingState.MOTTATT
            endringslogginnslag.any {
                it.felt === Felt.KJENNELSE_MOTTATT
                        && behandlingEndretEvent.behandling.type == Type.ANKE_I_TRYGDERETTEN
            } -> BehandlingState.MOTTATT_FRA_TR

            endringslogginnslag.any { it.felt === Felt.TILDELT_SAKSBEHANDLERIDENT } -> BehandlingState.TILDELT_SAKSBEHANDLER
            endringslogginnslag.any {
                it.felt === Felt.AVSLUTTET_AV_SAKSBEHANDLER
                        && behandlingEndretEvent.behandling.type == Type.ANKE
                        && behandlingEndretEvent.behandling.currentDelbehandling().utfall !in utfallToTrygderetten
            } -> BehandlingState.AVSLUTTET

            endringslogginnslag.any {
                it.felt === Felt.AVSLUTTET_AV_SAKSBEHANDLER
                        && behandlingEndretEvent.behandling.type == Type.ANKE_I_TRYGDERETTEN
                        && behandlingEndretEvent.behandling.currentDelbehandling().utfall in utfallToNewAnkebehandling
            } -> BehandlingState.NY_ANKEBEHANDLING_I_KA

            endringslogginnslag.any {
                it.felt === Felt.AVSLUTTET_AV_SAKSBEHANDLER
                        && behandlingEndretEvent.behandling.type != Type.ANKE
            } -> BehandlingState.AVSLUTTET

            endringslogginnslag.any {
                it.felt === Felt.AVSLUTTET_AV_SAKSBEHANDLER
                        && behandlingEndretEvent.behandling.type == Type.ANKE
                        && behandlingEndretEvent.behandling.currentDelbehandling().utfall in utfallToTrygderetten
            } -> BehandlingState.SENDT_TIL_TR

            else -> BehandlingState.UKJENT.also {
                logger.warn(
                    "unknown state for behandling with id {}",
                    endringslogginnslag.first().behandlingId
                )
            }
        }
    }

    private fun createStatistikkTilDVH(
        eventId: UUID,
        behandling: Behandling,
        behandlingState: BehandlingState
    ): StatistikkTilDVH {
        return StatistikkTilDVH(
            eventId = eventId,
            behandlingId = behandling.dvhReferanse ?: behandling.kildeReferanse,
            behandlingIdKabal = behandling.id.toString(),
            //Means enhetTildeltDato
            behandlingStartetKA = behandling.tildeling?.tidspunkt?.toLocalDate(),
            behandlingStatus = behandlingState,
            behandlingType = getBehandlingTypeName(behandling.type),
            //Means medunderskriver
            beslutter = behandling.currentDelbehandling().medunderskriver?.saksbehandlerident,
            endringstid = getFunksjoneltEndringstidspunkt(behandling, behandlingState),
            hjemmel = behandling.currentDelbehandling().hjemler.map { it.toSearchableString() },
            klager = getPart(behandling.klager.partId.type, behandling.klager.partId.value),
            opprinneligFagsaksystem = behandling.sakFagsystem.navn,
            overfoertKA = behandling.mottattKlageinstans.toLocalDate(),
            resultat = getResultat(behandling),
            sakenGjelder = getPart(behandling.sakenGjelder.partId.type, behandling.sakenGjelder.partId.value),
            saksbehandler = behandling.tildeling?.saksbehandlerident,
            saksbehandlerEnhet = behandling.tildeling?.enhet,
            tekniskTid = behandling.modified,
            vedtaksdato = behandling.currentDelbehandling().avsluttetAvSaksbehandler?.toLocalDate(),
            ytelseType = behandling.ytelse.navn,
        )
    }

    private fun getBehandlingTypeName(type: Type): String =
        if (type == Type.ANKE_I_TRYGDERETTEN) {
            Type.ANKE.navn
        } else {
            type.navn
        }

    //Resultat should only be relevant if avsluttetAvSaksbehandler
    private fun getResultat(behandling: Behandling): String? =
        if (behandling.avsluttetAvSaksbehandler != null) {
            behandling.currentDelbehandling().utfall?.name?.let { ExternalUtfall.valueOf(it).navn }
        } else {
            null
        }

    private fun getFunksjoneltEndringstidspunkt(
        behandling: Behandling,
        behandlingState: BehandlingState
    ): LocalDateTime {
        return when (behandlingState) {
            BehandlingState.MOTTATT -> behandling.mottattKlageinstans
            BehandlingState.TILDELT_SAKSBEHANDLER -> behandling.tildeling?.tidspunkt
                ?: throw RuntimeException("tildelt mangler")

            BehandlingState.AVSLUTTET, BehandlingState.NY_ANKEBEHANDLING_I_KA -> behandling.currentDelbehandling().avsluttetAvSaksbehandler
                ?: throw RuntimeException("avsluttetAvSaksbehandler mangler")

            BehandlingState.UKJENT -> {
                logger.warn("Unknown funksjoneltEndringstidspunkt. Missing state.")
                LocalDateTime.now()
            }

            BehandlingState.SENDT_TIL_TR -> behandling.currentDelbehandling().avsluttetAvSaksbehandler
                ?: throw RuntimeException("avsluttetAvSaksbehandler mangler")

            BehandlingState.MOTTATT_FRA_TR -> {
                behandling as AnkeITrygderettenbehandling
                behandling.kjennelseMottatt ?: throw RuntimeException("kjennelseMottatt mangler")
            }
        }
    }

    private fun getPart(type: PartIdType, value: String) =
        when (type) {
            PartIdType.PERSON -> {
                StatistikkTilDVH.Part(
                    verdi = value,
                    type = StatistikkTilDVH.PartIdType.PERSON
                )
            }

            PartIdType.VIRKSOMHET -> {
                StatistikkTilDVH.Part(
                    verdi = value,
                    type = StatistikkTilDVH.PartIdType.VIRKSOMHET
                )
            }
        }

    private fun Registreringshjemmel.toSearchableString(): String {
        return "${lovKilde.navn}-${spesifikasjon}"
    }
}