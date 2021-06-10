package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.KlagebehandlingEditableFieldsInput
import no.nav.klage.oppgave.api.view.TilknyttetDokument
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Grunn
import no.nav.klage.oppgave.domain.kodeverk.Hjemmel
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KlagebehandlingEditableFieldsFacade(
    private val klagebehandlingService: KlagebehandlingService,
    private val vedtakService: VedtakService
) {

    fun updateEditableFields(
        klagebehandlingId: UUID,
        input: KlagebehandlingEditableFieldsInput,
        innloggetIdent: String
    ): Klagebehandling {
        val klagebehandling =
            klagebehandlingService.getKlagebehandlingForUpdate(klagebehandlingId, input.klagebehandlingVersjon)

        dirtyCheckAndUpdateInternVurdering(input, klagebehandling, innloggetIdent)
        //dirtyCheckAndUpdateSendTilbakemelding(input, klagebehandling, innloggetIdent)
        //dirtyCheckAndUpdateTilbakemelding(input, klagebehandling, innloggetIdent)
        dirtyCheckAndUpdateUtfall(input, klagebehandling, innloggetIdent)
        dirtyCheckAndUpdateGrunn(input, klagebehandling, innloggetIdent)
        dirtyCheckAndUpdateHjemlerInVedtak(input, klagebehandling, innloggetIdent)
        dirtyCheckAndUpdateDokumentReferanser(input, klagebehandling, innloggetIdent)
        return klagebehandling
    }

    private fun dirtyCheckAndUpdateDokumentReferanser(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.tilknyttedeDokumenter
        val gammelVerdi =
            klagebehandling.saksdokumenter.map { TilknyttetDokument(it.journalpostId, it.dokumentInfoId) }.toSet()
        if (isDirty(gammelVerdi, nyVerdi)) {
            val nyeDokumenter = nyVerdi - gammelVerdi
            val slettedeDokumenter = gammelVerdi - nyVerdi
            nyeDokumenter.forEach {
                klagebehandlingService.connectDokumentToKlagebehandling(
                    klagebehandling,
                    it.journalpostId,
                    it.dokumentInfoId,
                    innloggetIdent
                )
            }
            slettedeDokumenter.forEach {
                klagebehandlingService.disconnectDokumentFromKlagebehandling(
                    klagebehandling,
                    it.journalpostId,
                    it.dokumentInfoId,
                    innloggetIdent
                )
            }

        }
    }

    private fun dirtyCheckAndUpdateHjemlerInVedtak(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.hjemler?.map { Hjemmel.of(it) }?.toSet() ?: emptySet()
        val gammelVerdi = klagebehandling.vedtak.first().hjemler
        if (isDirty(gammelVerdi, nyVerdi)) {
            vedtakService.setHjemler(
                klagebehandling,
                klagebehandling.vedtak.first().id,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    /*
    private fun dirtyCheckAndUpdateTilbakemelding(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.tilbakemelding
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.tilbakemelding
        if (isDirty(gammelVerdi, nyVerdi)) {
            klagebehandlingService.setKvalitetsvurderingTilbakemelding(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateSendTilbakemelding(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.sendTilbakemelding
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.sendTilbakemelding
        if (isDirty(gammelVerdi, nyVerdi)) {
            klagebehandlingService.setKvalitetsvurderingSendTilbakemelding(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }
     */

    private fun dirtyCheckAndUpdateInternVurdering(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.internVurdering.ifBlank { null }
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.internVurdering
        if (isDirty(gammelVerdi, nyVerdi)) {
            klagebehandlingService.setKvalitetsvurderingInternVurdering(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateGrunn(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.grunn?.let { Grunn.of(it) }
        val gammelVerdi = klagebehandling.vedtak.first().grunn
        if (isDirty(gammelVerdi, nyVerdi)) {
            vedtakService.setGrunn(
                klagebehandling,
                klagebehandling.vedtak.first().id,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateUtfall(
        input: KlagebehandlingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.utfall?.let { Utfall.of(it) }
        val gammelVerdi = klagebehandling.vedtak.first().utfall
        if (isDirty(gammelVerdi, nyVerdi)) {
            vedtakService.setUtfall(
                klagebehandling,
                klagebehandling.vedtak.first().id,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    fun isDirty(gammelVerdi: Any?, nyVerdi: Any?): Boolean {
        return gammelVerdi != nyVerdi
    }
}