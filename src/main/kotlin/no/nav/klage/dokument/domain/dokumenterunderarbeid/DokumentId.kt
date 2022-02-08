package no.nav.klage.dokument.domain.dokumenterunderarbeid

import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class DokumentId(
    @Column(name = "id")
    open var id: UUID,
) : Serializable