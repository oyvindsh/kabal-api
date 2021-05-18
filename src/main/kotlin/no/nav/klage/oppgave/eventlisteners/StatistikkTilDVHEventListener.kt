package no.nav.klage.oppgave.eventlisteners

import no.nav.klage.oppgave.domain.kafka.KlageStatistikkTilDVH
import no.nav.klage.oppgave.domain.klage.Endringslogginnslag
import no.nav.klage.oppgave.domain.klage.Felt
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.events.KlagebehandlingEndretEvent
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.service.StatistikkTilDVHKafkaProducer
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StatistikkTilDVHEventListener(
    private val statistikkTilDVHKafkaProducer: StatistikkTilDVHKafkaProducer,
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val mottakRepository: MottakRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @EventListener
    fun klagebehandlingEndretEventToDVH(klagebehandlingEndretEvent: KlagebehandlingEndretEvent) {
        logger.debug("Received KlagebehandlingEndretEvent for klagebehandlingId ${klagebehandlingEndretEvent.klagebehandling.id} in StatistikkTilDVHEventListener")

        val klagebehandling =
            klagebehandlingRepository.findById(klagebehandlingEndretEvent.klagebehandling.id).orElseThrow()

        val mottak = mottakRepository.getOne(klagebehandling.mottakId)

        if (shouldSendStats(klagebehandlingEndretEvent.endringslogginnslag)) {
            val klageStatistikkTilDVH = createDTO(klagebehandling, mottak)
            statistikkTilDVHKafkaProducer.sendStatistikkTilDVH(klageStatistikkTilDVH)
        }
    }

    private fun shouldSendStats(endringslogginnslag: List<Endringslogginnslag>): Boolean {
        return endringslogginnslag.isEmpty() ||
                endringslogginnslag.any { it.felt === Felt.TILDELT_SAKSBEHANDLERIDENT || it.felt === Felt.SLUTTFOERT }
    }

    private fun createDTO(klagebehandling: Klagebehandling, mottak: Mottak): KlageStatistikkTilDVH {
        val vedtak = klagebehandling.vedtak.firstOrNull()
        val now = LocalDateTime.now()
        return KlageStatistikkTilDVH(
            ansvarligEnhetKode = klagebehandling.tildeltEnhet,
            ansvarligEnhetType = "NORG",
            behandlingId = mottak.dvhReferanse,
            behandlingIdKabal = klagebehandling.id.toString(),
            behandlingStartetKA = klagebehandling.startet,
            behandlingStatus = "TODO",
            behandlingType = "TODO",
            beslutter = klagebehandling.medunderskriverident,
            endringstid = now,
            hjemmel = klagebehandling.hjemler.joinToString(separator = ",") { it.toSearchableString() },
            klager = "TODO",
            omgjoeringsgrunn = vedtak?.grunn?.navn,
            opprinneligFagsaksystem = mottak.kildesystem.navn,
            overfoertKA = mottak.created.toLocalDate(),
            resultat = vedtak?.utfall?.navn,
            sakenGjelder = "TODO",
            saksbehandler = klagebehandling.tildeltSaksbehandlerident,
            saksbehandlerEnhet = klagebehandling.tildeltEnhet,
            tekniskTid = now,
            vedtakId = vedtak?.id.toString(),
            vedtaksdato = vedtak?.finalized?.toLocalDate(),
            versjon = 1,
            ytelseType = "TODO"
        )
    }
}