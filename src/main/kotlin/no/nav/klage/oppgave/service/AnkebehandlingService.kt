package no.nav.klage.oppgave.service

import jakarta.transaction.Transactional
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.clients.kaka.KakaApiGateway
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.repositories.AnkebehandlingRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period
import java.util.*

@Service
@Transactional
class AnkebehandlingService(
    private val ankebehandlingRepository: AnkebehandlingRepository,
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val kakaApiGateway: KakaApiGateway,
    private val dokumentService: DokumentService,
    private val vedtakService: VedtakService,
    private val behandlingService: BehandlingService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    @Value("#{T(java.time.LocalDate).parse('\${KAKA_VERSION_2_DATE}')}")
    private val kakaVersion2Date: LocalDate,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        const val SYSTEMBRUKER = "SYSTEMBRUKER"
    }

    fun getAnkebehandlingFromMottakId(mottakId: UUID): Ankebehandling? {
        return ankebehandlingRepository.findByMottakId(mottakId)
    }

    fun createAnkebehandlingFromMottak(mottak: Mottak): Ankebehandling {
        val kvalitetsvurderingVersion = getKakaVersion()

        val ankebehandling = ankebehandlingRepository.save(
            Ankebehandling(
                klager = mottak.klager.copy(),
                sakenGjelder = mottak.sakenGjelder?.copy() ?: mottak.klager.toSakenGjelder(),
                ytelse = mottak.ytelse,
                type = mottak.type,
                kildeReferanse = mottak.kildeReferanse,
                dvhReferanse = mottak.dvhReferanse,
                fagsystem = mottak.fagsystem,
                fagsakId = mottak.fagsakId,
                innsendt = mottak.innsendtDato,
                mottattKlageinstans = mottak.sakMottattKaDato,
                tildeling = null,
                frist = mottak.generateFrist(),
                mottakId = mottak.id,
                delbehandlinger = setOf(Delbehandling()),
                saksdokumenter = dokumentService.createSaksdokumenterFromJournalpostIdSet(mottak.mottakDokument.map { it.journalpostId }),
                kakaKvalitetsvurderingId = kakaApiGateway.createKvalitetsvurdering(kvalitetsvurderingVersion = kvalitetsvurderingVersion).kvalitetsvurderingId,
                kakaKvalitetsvurderingVersion = kvalitetsvurderingVersion,
                hjemler = createHjemmelSetFromMottak(mottak.hjemler),
                klageBehandlendeEnhet = mottak.forrigeBehandlendeEnhet,
                klagebehandlingId = mottak.forrigeBehandlingId,
            )
        )
        logger.debug("Created ankebehandling ${ankebehandling.id} for mottak ${mottak.id}")

        if (mottak.forrigeBehandlingId != null) {
            val klagebehandling = klagebehandlingRepository.getReferenceById(mottak.forrigeBehandlingId)
            val klagebehandlingDokumenter = klagebehandling.saksdokumenter

            logger.debug("Adding saksdokumenter from klagebehandling ${mottak.forrigeBehandlingId} to ankebehandling ${ankebehandling.id}")
            klagebehandlingDokumenter.forEach {
                behandlingService.connectDokumentToBehandling(
                    behandlingId = ankebehandling.id,
                    journalpostId = it.journalpostId,
                    dokumentInfoId = it.dokumentInfoId,
                    saksbehandlerIdent = SYSTEMBRUKER,
                    systemUserContext = true,
                )
            }
        }

        applicationEventPublisher.publishEvent(
            BehandlingEndretEvent(
                behandling = ankebehandling,
                endringslogginnslag = emptyList()
            )
        )
        return ankebehandling
    }

    private fun getKakaVersion(): Int {
        val kvalitetsvurderingVersion = if (LocalDate.now() >= kakaVersion2Date) {
            2
        } else {
            1
        }
        return kvalitetsvurderingVersion
    }

    fun createAnkebehandlingFromAnkeITrygderettenbehandling(ankeITrygderettenbehandling: AnkeITrygderettenbehandling): Ankebehandling {
        val kvalitetsvurderingVersion = getKakaVersion()


        val ankebehandling = ankebehandlingRepository.save(
            Ankebehandling(
                klager = ankeITrygderettenbehandling.klager.copy(),
                sakenGjelder = ankeITrygderettenbehandling.sakenGjelder.copy(),
                ytelse = ankeITrygderettenbehandling.ytelse,
                type = Type.ANKE,
                kildeReferanse = ankeITrygderettenbehandling.kildeReferanse,
                dvhReferanse = ankeITrygderettenbehandling.dvhReferanse,
                fagsystem = ankeITrygderettenbehandling.fagsystem,
                fagsakId = ankeITrygderettenbehandling.fagsakId,
                innsendt = ankeITrygderettenbehandling.mottattKlageinstans.toLocalDate(),
                mottattKlageinstans = ankeITrygderettenbehandling.mottattKlageinstans,
                tildeling = null,
                frist = LocalDate.now() + Period.ofWeeks(12),
                delbehandlinger = setOf(Delbehandling()),
                kakaKvalitetsvurderingId = kakaApiGateway.createKvalitetsvurdering(kvalitetsvurderingVersion = kvalitetsvurderingVersion).kvalitetsvurderingId,
                kakaKvalitetsvurderingVersion = kvalitetsvurderingVersion,
                hjemler = ankeITrygderettenbehandling.hjemler,
                klageBehandlendeEnhet = ankeITrygderettenbehandling.tildeling?.enhet!!,
            )
        )
        logger.debug("Created ankebehandling ${ankebehandling.id} from ankeITrygderettenbehandling ${ankeITrygderettenbehandling.id}")

        ankeITrygderettenbehandling.saksdokumenter.forEach {
            behandlingService.connectDokumentToBehandling(
                behandlingId = ankebehandling.id,
                journalpostId = it.journalpostId,
                dokumentInfoId = it.dokumentInfoId,
                saksbehandlerIdent = SYSTEMBRUKER,
                systemUserContext = true,
            )
        }

        applicationEventPublisher.publishEvent(
            BehandlingEndretEvent(
                behandling = ankebehandling,
                endringslogginnslag = listOfNotNull(
                    Endringslogginnslag.endringslogg(
                        saksbehandlerident = ankeITrygderettenbehandling.tildeling!!.saksbehandlerident,
                        felt = Felt.ANKEBEHANDLING_OPPRETTET_BASERT_PAA_ANKE_I_TRYGDERETTEN,
                        fraVerdi = null,
                        tilVerdi = "Opprettet",
                        behandlingId = ankebehandling.id,
                        tidspunkt = ankebehandling.created,
                    )
                )
            )
        )

        //TODO: Undersøk om vi skal sende noen infomelding om at dette har skjedd

        return ankebehandling
    }

    private fun createHjemmelSetFromMottak(hjemler: Set<MottakHjemmel>): MutableSet<Hjemmel> =
        hjemler.map { Hjemmel.of(it.hjemmelId) }.toMutableSet()
}