package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.mapper.toBrevElementView
import no.nav.klage.oppgave.api.mapper.toVedtaksBrevView
import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElementView
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrev
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrevView
import no.nav.klage.oppgave.domain.vedtaksbrev.updateFields
import no.nav.klage.oppgave.repositories.BrevElementRepository
import no.nav.klage.oppgave.repositories.VedtaksBrevRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class VedtaksBrevService(
    private val vedtaksBrevRepository: VedtaksBrevRepository,
    private val brevElementRepository: BrevElementRepository
) {
    fun createVedtaksBrev(vedtaksBrevView: VedtaksBrevView): VedtaksBrevView {
        val newVedtaksBrev = VedtaksBrev(
            klagebehandlingId = vedtaksBrevView.klagebehandlingId,
            brevMal = vedtaksBrevView.brevMal
        )
        newVedtaksBrev.brevElements = newVedtaksBrev.brevMal.elementOrder.map {
            it.generateDefaultBrevElement(newVedtaksBrev.id)
        }
        vedtaksBrevRepository.save(newVedtaksBrev)
        return newVedtaksBrev.toVedtaksBrevView()
    }

    fun getVedtaksBrev(brevId: UUID): VedtaksBrevView {
        val brev = vedtaksBrevRepository.getOne(brevId)
        return brev.toVedtaksBrevView()
    }

    fun getVedtaksBrevByKlagebehandlingId(klagebehandlingId: UUID): List<VedtaksBrevView> {
        val brevResults = vedtaksBrevRepository.findByKlagebehandlingId(klagebehandlingId)
        return brevResults.map { it.toVedtaksBrevView() }
    }

    fun deleteVedtaksbrev(brevId: UUID) {
        vedtaksBrevRepository.deleteById(brevId)
    }

    fun updateBrevElement(brevId: UUID, inputElement: BrevElementView): BrevElementView {
        val existingElement = brevElementRepository.findByBrevIdAndKey(brevId, inputElement.key)
        existingElement.updateFields(inputElement)
        return existingElement.toBrevElementView()
    }
}