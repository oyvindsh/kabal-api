package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElement
import no.nav.klage.oppgave.domain.vedtaksbrev.BrevElementView
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrev
import no.nav.klage.oppgave.domain.vedtaksbrev.VedtaksBrevView
import no.nav.klage.oppgave.domain.vedtaksbrev.enums.BrevElementKey


fun VedtaksBrev.toVedtaksBrevView(): VedtaksBrevView {
    return VedtaksBrevView(
        id = id,
        klagebehandlingId = klagebehandlingId,
        brevMal = brevMal,
        elements = brevElements
            ?.map { it.toBrevElementView() }
            ?.sortedWith(BrevElementComparator(brevMal.elementOrder))
    )
}

fun BrevElement.toBrevElementView(): BrevElementView {
    return BrevElementView(
        key,
        displayText,
        content,
        elementInputType = elementInputType,
        placeholderText = key.getPlaceholderText()
    )
}

class BrevElementComparator(elementOrder: List<BrevElementKey>) : Comparator<BrevElementView> {
    private val currentElementOrder = elementOrder
    override fun compare(a: BrevElementView, b: BrevElementView): Int {
        return currentElementOrder.indexOf(a.key) - currentElementOrder.indexOf(b.key)
    }
}