package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.EditerbareFelterInput
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Utfall
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class KlagebehandlingMegaPutFacade(
    private val klagebehandlingService: KlagebehandlingService,
    private val vedtakService: VedtakService
) {

    fun updateEditerbareFelter(
        klagebehandlingId: UUID,
        input: EditerbareFelterInput,
        innloggetIdent: String
    ): Klagebehandling {
        val klagebehandling =
            klagebehandlingService.getKlagebehandlingForUpdate(klagebehandlingId, input.klagebehandlingVersjon)
        dirtyCheckAndUpdateUtfall(input, klagebehandling, innloggetIdent)
        //TODO: Alle de andre feltene også..
        return klagebehandling
    }

    private fun dirtyCheckAndUpdateUtfall(
        input: EditerbareFelterInput,
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
        if (gammelVerdi == null && nyVerdi == null) {
            return false
        }
        if (gammelVerdi == null && nyVerdi != null) {
            return true
        }
        if (gammelVerdi != null && nyVerdi == null) {
            return true
        }
        if (gammelVerdi == nyVerdi) {
            return false
        }
        return true
    }
}