package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.*
import no.nav.klage.kodeverk.PartIdType

@Embeddable
data class Prosessfullmektig(
    @Embedded
    @AttributeOverrides(
        value = [
            AttributeOverride(name = "type", column = Column(name = "prosessfullmektig_type")),
            AttributeOverride(name = "value", column = Column(name = "prosessfullmektig_value"))
        ]
    )
    val partId: PartId,
    @Column(name = "skal_parten_motta_kopi")
    val skalPartenMottaKopi: Boolean
) {
    fun isPerson() = partId.type == PartIdType.PERSON
}
