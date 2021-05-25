package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.PartIdType
import no.nav.klage.oppgave.domain.kodeverk.PartIdTypeConverter
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Embeddable

@Embeddable
data class PartId(
    @Column(name = "type")
    @Convert(converter = PartIdTypeConverter::class)
    var type: PartIdType,
    @Column(name = "value")
    val value: String,
)
