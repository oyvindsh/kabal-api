package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.api.mapper.BehandlingMapper
import no.nav.klage.oppgave.api.view.kabin.*
import no.nav.klage.oppgave.domain.klage.Ankebehandling
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Mottak
import no.nav.klage.oppgave.domain.klage.MottakDokumentType
import no.nav.klage.oppgave.exceptions.BehandlingNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class KabinApiService(
    private val behandlingMapper: BehandlingMapper,
    private val dokumentService: DokumentService,
    private val saksbehandlerService: SaksbehandlerService,
    private val mottakService: MottakService,
    private val ankebehandlingService: AnkebehandlingService,
    private val behandlingService: BehandlingService,
    private val klagebehandlingService: KlagebehandlingService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService
) {

    fun createAnke(input: CreateAnkeBasedOnKabinInput): CreatedAnkeResponse {
        val mottakId = mottakService.createAnkeMottakFromKabinInput(input = input)
        if (input.saksbehandlerIdent != null) {
            val ankebehandling = ankebehandlingService.getAnkebehandlingFromMottakId(mottakId)
            behandlingService.setSaksbehandler(
                behandlingId = ankebehandling!!.id,
                tildeltSaksbehandlerIdent = input.saksbehandlerIdent,
                enhetId = saksbehandlerService.getEnhetForSaksbehandler(
                    input.saksbehandlerIdent
                ).enhetId,
                utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            )
        }
        return CreatedAnkeResponse(mottakId = mottakId)
    }

    fun createAnkeFromCompleteKabinInput(input: CreateAnkeBasedOnCompleteKabinInput): CreatedAnkeResponse {
        val mottakId = mottakService.createAnkeMottakFromCompleteKabinInput(input = input)
        if (input.saksbehandlerIdent != null) {
            val ankebehandling = ankebehandlingService.getAnkebehandlingFromMottakId(mottakId)
            behandlingService.setSaksbehandler(
                behandlingId = ankebehandling!!.id,
                tildeltSaksbehandlerIdent = input.saksbehandlerIdent,
                enhetId = saksbehandlerService.getEnhetForSaksbehandler(
                    input.saksbehandlerIdent
                ).enhetId,
                utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            )
        }
        return CreatedAnkeResponse(mottakId = mottakId)
    }

    fun getCreatedAnkebehandlingStatus(
        mottakId: UUID
    ): CreatedAnkebehandlingStatusForKabin {
        val mottak =
            mottakService.getMottak(mottakId = mottakId) ?: throw RuntimeException("mottak not found for id $mottakId")

        val ankebehandling = ankebehandlingService.getAnkebehandlingFromMottakId(mottakId)
            ?: throw BehandlingNotFoundException("anke not found")

        val completedKlagebehandling =
            klagebehandlingService.findCompletedKlagebehandlingById(ankebehandling.klagebehandlingId!!)

        return getCreatedAnkebehandlingStatusForKabin(
            completedKlagebehandling = completedKlagebehandling,
            ankebehandling = ankebehandling,
            mottak = mottak,
        )
    }

    fun createKlage(
        input: CreateKlageBasedOnKabinInput
    ): CreatedKlageResponse {
        val mottakId = mottakService.createKlageMottakFromKabinInput(klageInput = input)
        if (input.saksbehandlerIdent != null) {
            val ankebehandling = klagebehandlingService.getKlagebehandlingFromMottakId(mottakId)
            behandlingService.setSaksbehandler(
                behandlingId = ankebehandling!!.id,
                tildeltSaksbehandlerIdent = input.saksbehandlerIdent,
                enhetId = saksbehandlerService.getEnhetForSaksbehandler(
                    input.saksbehandlerIdent
                ).enhetId,
                utfoerendeSaksbehandlerIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            )
        }
        return CreatedKlageResponse(mottakId = mottakId)
    }

    fun getCreatedKlagebehandlingStatus(
        mottakId: UUID
    ): CreatedKlagebehandlingStatusForKabin {
        val mottak =
            mottakService.getMottak(mottakId = mottakId) ?: throw RuntimeException("mottak not found for id $mottakId")
        val klagebehandling = klagebehandlingService.getKlagebehandlingFromMottakId(mottakId)
            ?: throw BehandlingNotFoundException("klage not found")

        return getCreatedKlagebehandlingStatusForKabin(
            klagebehandling = klagebehandling,
            mottak = mottak
        )
    }

    private fun getCreatedAnkebehandlingStatusForKabin(
        completedKlagebehandling: CompletedKlagebehandling,
        ankebehandling: Ankebehandling,
        mottak: Mottak,
    ): CreatedAnkebehandlingStatusForKabin {
        return CreatedAnkebehandlingStatusForKabin(
            typeId = Type.ANKE.id,
            behandlingId = completedKlagebehandling.behandlingId,
            ytelseId = completedKlagebehandling.ytelseId,
            utfallId = completedKlagebehandling.utfallId,
            vedtakDate = completedKlagebehandling.vedtakDate,
            sakenGjelder = behandlingMapper.getSakenGjelderView(ankebehandling.sakenGjelder),
            klager = behandlingMapper.getPartView(ankebehandling.klager),
            fullmektig = ankebehandling.klager.prosessfullmektig?.let { behandlingMapper.getPartView(it) },
            tilknyttedeDokumenter = completedKlagebehandling.tilknyttedeDokumenter,
            mottattNav = ankebehandling.mottattKlageinstans.toLocalDate(),
            frist = ankebehandling.frist!!,
            sakFagsakId = completedKlagebehandling.fagsakId,
            fagsakId = completedKlagebehandling.fagsakId,
            sakFagsystem = completedKlagebehandling.fagsystem,
            fagsystem = completedKlagebehandling.fagsystem,
            fagsystemId = completedKlagebehandling.fagsystemId,
            journalpost = dokumentService.getDokumentReferanse(
                journalpostId = mottak.mottakDokument.find { it.type == MottakDokumentType.BRUKERS_ANKE }!!.journalpostId,
                behandling = ankebehandling
            ),
            tildeltSaksbehandler = ankebehandling.tildeling?.saksbehandlerident?.let {
                TildeltSaksbehandler(
                    navIdent = it,
                    navn = saksbehandlerService.getNameForIdent(it),
                )
            },
        )
    }

    private fun getCreatedKlagebehandlingStatusForKabin(
        klagebehandling: Klagebehandling,
        mottak: Mottak,
    ): CreatedKlagebehandlingStatusForKabin {
        return CreatedKlagebehandlingStatusForKabin(
            typeId = Type.KLAGE.id,
            behandlingId = klagebehandling.id,
            ytelseId = klagebehandling.ytelse.id,
            sakenGjelder = behandlingMapper.getSakenGjelderView(klagebehandling.sakenGjelder),
            klager = behandlingMapper.getPartView(klagebehandling.klager),
            fullmektig = klagebehandling.klager.prosessfullmektig?.let { behandlingMapper.getPartView(it) },
            mottattVedtaksinstans = klagebehandling.mottattVedtaksinstans,
            mottattKlageinstans = klagebehandling.mottattKlageinstans.toLocalDate(),
            frist = klagebehandling.frist!!,
            fagsakId = klagebehandling.fagsakId,
            fagsystemId = klagebehandling.fagsystem.id,
            journalpost = dokumentService.getDokumentReferanse(
                journalpostId = mottak.mottakDokument.find { it.type == MottakDokumentType.BRUKERS_KLAGE }!!.journalpostId,
                behandling = klagebehandling
            ),
            kildereferanse = mottak.kildeReferanse,
            tildeltSaksbehandler = klagebehandling.tildeling?.saksbehandlerident?.let {
                TildeltSaksbehandler(
                    navIdent = it,
                    navn = saksbehandlerService.getNameForIdent(it),
                )
            },
        )
    }
}