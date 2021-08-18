package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.KvalitetsvurderingEditableFieldsInput
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikOversendelsesbrev
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikUtredning
import no.nav.klage.oppgave.domain.kodeverk.KvalitetsavvikVedtak
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KvalitetsvurderingEditableFieldsFacade(
    private val kvalitetsvurderingService: KvalitetsvurderingService,
    private val klagebehandlingService: KlagebehandlingService
) {

    fun updateEditableFields(
        klagebehandlingId: UUID,
        input: KvalitetsvurderingEditableFieldsInput,
        innloggetIdent: String
    ): Klagebehandling {
        val klagebehandlingForUpdate =
            klagebehandlingService.getKlagebehandlingForUpdate(klagebehandlingId, input.klagebehandlingVersjon)

        dirtyCheckAndUpdateInkluderteDatoForKlage(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateInkluderteDatoForVedtak(input, klagebehandlingForUpdate, innloggetIdent)

        dirtyCheckAndUpdateOversendelsesbrevBra(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateAvvikOversendelsesbrev(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateKommentarOversendelsesbrev(input, klagebehandlingForUpdate, innloggetIdent)

        dirtyCheckAndUpdateUtredningBra(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateAvvikUtredning(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateKommentarUtredning(input, klagebehandlingForUpdate, innloggetIdent)

        dirtyCheckAndUpdateVedtakBra(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateAvvikVedtak(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateKommentarVedtak(input, klagebehandlingForUpdate, innloggetIdent)

        dirtyCheckAndUpdateAvvikStorKonsekvens(input, klagebehandlingForUpdate, innloggetIdent)
        dirtyCheckAndUpdateBrukSomEksempelIOpplaering(input, klagebehandlingForUpdate, innloggetIdent)
        return klagebehandlingForUpdate
    }

    private fun dirtyCheckAndUpdateInkluderteDatoForKlage(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandlingForUpdate: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.inkluderteDatoForKlage
        val gammelVerdi = klagebehandlingForUpdate.kvalitetsvurdering?.inkluderteDatoForKlage
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setInkluderteDatoForKlage(
                klagebehandlingForUpdate,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateInkluderteDatoForVedtak(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.inkluderteDatoForVedtak
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.inkluderteDatoForVedtak
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setInkluderteDatoForVedtak(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateOversendelsesbrevBra(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kvalitetOversendelsesbrevBra
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.oversendelsesbrevBra
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setOversendelsesbrevBra(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateAvvikOversendelsesbrev(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kvalitetsavvikOversendelsesbrev.map { KvalitetsavvikOversendelsesbrev.of(it) }.toSet()
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.kvalitetsavvikOversendelsesbrev
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setKvalitetsavvikOversendelsesbrev(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateKommentarOversendelsesbrev(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kommentarOversendelsesbrev
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.kommentarOversendelsesbrev
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setKommentarOversendelsesbrev(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateUtredningBra(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kvalitetUtredningBra
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.utredningBra
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setUtredningBra(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateAvvikUtredning(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kvalitetsavvikUtredning.map { KvalitetsavvikUtredning.of(it) }.toSet()
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.kvalitetsavvikUtredning
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setKvalitetsavvikUtredning(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateKommentarUtredning(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kommentarUtredning
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.kommentarUtredning
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setKommentarUtredning(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateVedtakBra(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kvalitetVedtakBra
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.vedtakBra
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setVedtakBra(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateAvvikVedtak(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kvalitetsavvikVedtak.map { KvalitetsavvikVedtak.of(it) }.toSet()
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.kvalitetsavvikVedtak
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setKvalitetsavvikVedtak(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateKommentarVedtak(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.kommentarVedtak
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.kommentarVedtak
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setKommentarVedtak(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateAvvikStorKonsekvens(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.avvikStorKonsekvens
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.avvikStorKonsekvens
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setAvvikStorKonsekvens(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    private fun dirtyCheckAndUpdateBrukSomEksempelIOpplaering(
        input: KvalitetsvurderingEditableFieldsInput,
        klagebehandling: Klagebehandling,
        innloggetIdent: String
    ) {
        val nyVerdi = input.brukSomEksempelIOpplaering
        val gammelVerdi = klagebehandling.kvalitetsvurdering?.brukSomEksempelIOpplaering
        if (isDirty(gammelVerdi, nyVerdi)) {
            kvalitetsvurderingService.setBrukSomEksempelIOpplaering(
                klagebehandling,
                nyVerdi,
                innloggetIdent
            )
        }
    }

    fun isDirty(gammelVerdi: Any?, nyVerdi: Any?): Boolean {
        return gammelVerdi != nyVerdi
    }
}