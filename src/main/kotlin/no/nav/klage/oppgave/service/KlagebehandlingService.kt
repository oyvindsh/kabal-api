package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Utfall
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.oppgave.api.mapper.BehandlingMapper
import no.nav.klage.oppgave.api.view.TilknyttetDokument
import no.nav.klage.oppgave.api.view.kabin.CompletedKlagebehandling
import no.nav.klage.oppgave.clients.kaka.KakaApiGateway
import no.nav.klage.oppgave.domain.events.BehandlingEndretEvent
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.exceptions.BehandlingNotFoundException
import no.nav.klage.oppgave.exceptions.PDLErrorException
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
@Transactional
class KlagebehandlingService(
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val dokumentService: DokumentService,
    private val kakaApiGateway: KakaApiGateway,
    @Value("#{T(java.time.LocalDate).parse('\${KAKA_VERSION_2_DATE}')}")
    private val kakaVersion2Date: LocalDate,
    private val behandlingMapper: BehandlingMapper,
    private val behandlingService: BehandlingService,
    private val mottakRepository: MottakRepository,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    var muligAnkeUtfall = setOf(
        Utfall.MEDHOLD,
        Utfall.DELVIS_MEDHOLD,
        Utfall.STADFESTELSE,
        Utfall.UGUNST,
        Utfall.AVVIST
    )

    @Transactional(propagation = Propagation.NEVER)
    fun findCompletedKlagebehandlingerByPartIdValue(
        partIdValue: String
    ): List<CompletedKlagebehandling> {
        return try {
            behandlingService.checkLeseTilgang(partIdValue)
            val results =
                klagebehandlingRepository.getAnkemuligheter(partIdValue)
            results.map { it.toCompletedKlagebehandling() }
        } catch (pdlee: PDLErrorException) {
            logger.warn("Returning empty list of CompletedKlagebehandling b/c pdl gave error response. Check secure logs")
            emptyList()
        }
    }

    fun findCompletedKlagebehandlingById(
        klagebehandlingId: UUID
    ): CompletedKlagebehandling {
        val behandling = klagebehandlingRepository.findByIdAndDelbehandlingerAvsluttetIsNotNull(klagebehandlingId)
        if (behandling != null) {
            behandlingService.checkLeseTilgang(behandling)
            return behandling.toCompletedKlagebehandling()
        } else {
            throw BehandlingNotFoundException("Completed klagebehandling with id $klagebehandlingId not found")
        }
    }

    private fun Klagebehandling.toCompletedKlagebehandling(): CompletedKlagebehandling = CompletedKlagebehandling(
        behandlingId = id,
        ytelseId = ytelse.id,
        utfallId = currentDelbehandling().utfall!!.id,
        vedtakDate = currentDelbehandling().avsluttetAvSaksbehandler!!,
        sakenGjelder = behandlingMapper.getSakenGjelderView(sakenGjelder),
        klager = behandlingMapper.getKlagerView(klager),
        fullmektig = klager.prosessfullmektig?.let { behandlingMapper.getProsessfullmektigView(it) },
        tilknyttedeDokumenter = saksdokumenter.map {
            TilknyttetDokument(
                journalpostId = it.journalpostId,
                dokumentInfoId = it.dokumentInfoId
            )
        },
        sakFagsakId = fagsakId,
        fagsakId = fagsakId,
        sakFagsystem = fagsystem,
        fagsystem = fagsystem,
        fagsystemId = fagsystem.id,
        klageBehandlendeEnhet = tildeling!!.enhet!!,
    )

    fun findMuligAnkeByPartId(
        partId: String
    ): List<MuligAnke> =
        klagebehandlingRepository.findByDelbehandlingerAvsluttetIsNotNull()
            .filter {
                it.klager.partId.value == partId &&
                        muligAnkeUtfall.contains(it.currentDelbehandling().utfall)
            }
            .map { it.toMuligAnke() }

    fun findMuligAnkeByPartIdAndKlagebehandlingId(
        partId: String,
        klagebehandlingId: UUID
    ): MuligAnke? {
        val klagebehandling =
            klagebehandlingRepository.findByIdAndDelbehandlingerAvsluttetIsNotNull(klagebehandlingId) ?: return null
        return if (
            klagebehandling.klager.partId.value == partId && muligAnkeUtfall.contains(klagebehandling.currentDelbehandling().utfall)
        ) {
            klagebehandling.toMuligAnke()
        } else {
            null
        }
    }

    fun createKlagebehandlingFromMottak(mottak: Mottak): Klagebehandling {
        val kvalitetsvurderingVersion = getKakaVersion()

        val klagebehandling = klagebehandlingRepository.save(
            Klagebehandling(
                klager = mottak.klager.copy(),
                sakenGjelder = mottak.sakenGjelder?.copy() ?: mottak.klager.toSakenGjelder(),
                ytelse = mottak.ytelse,
                type = mottak.type,
                kildeReferanse = mottak.kildeReferanse,
                dvhReferanse = mottak.dvhReferanse,
                fagsystem = mottak.fagsystem,
                fagsakId = mottak.fagsakId,
                innsendt = mottak.innsendtDato,
                mottattVedtaksinstans = mottak.brukersHenvendelseMottattNavDato,
                avsenderEnhetFoersteinstans = mottak.forrigeBehandlendeEnhet,
                avsenderSaksbehandleridentFoersteinstans = mottak.forrigeSaksbehandlerident,
                mottattKlageinstans = mottak.sakMottattKaDato,
                tildeling = null,
                frist = mottak.generateFrist(),
                mottakId = mottak.id,
                delbehandlinger = setOf(Delbehandling()),
                saksdokumenter = dokumentService.createSaksdokumenterFromJournalpostIdSet(mottak.mottakDokument.map { it.journalpostId }),
                kakaKvalitetsvurderingId = kakaApiGateway.createKvalitetsvurdering(kvalitetsvurderingVersion = kvalitetsvurderingVersion).kvalitetsvurderingId,
                kakaKvalitetsvurderingVersion = kvalitetsvurderingVersion,
                hjemler = createHjemmelSetFromMottak(mottak.hjemler),
                kommentarFraFoersteinstans = mottak.kommentar
            )
        )
        logger.debug("Created klagebehandling ${klagebehandling.id} for mottak ${mottak.id}")
        applicationEventPublisher.publishEvent(
            BehandlingEndretEvent(
                behandling = klagebehandling,
                endringslogginnslag = emptyList()
            )
        )
        return klagebehandling
    }

    fun getKlagebehandlingFromMottakId(mottakId: UUID): Klagebehandling? {
        return klagebehandlingRepository.findByMottakId(mottakId)
    }

    private fun getKakaVersion(): Int {
        val kvalitetsvurderingVersion = if (LocalDate.now() >= kakaVersion2Date) {
            2
        } else {
            1
        }
        return kvalitetsvurderingVersion
    }

    private fun createHjemmelSetFromMottak(hjemler: Set<MottakHjemmel>): MutableSet<Hjemmel> =
        hjemler.map { Hjemmel.of(it.hjemmelId) }.toMutableSet()


    private fun Klagebehandling.toMuligAnke(): MuligAnke = MuligAnke(
        this.id,
        this.ytelse.toTema(),
        this.currentDelbehandling().utfall!!,
        this.innsendt!!,
        this.currentDelbehandling().avsluttetAvSaksbehandler!!,
        this.klager.partId.value
    )
}