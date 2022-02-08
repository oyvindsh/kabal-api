package no.nav.klage.dokument.domain.dokumenterunderarbeid

import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class PersistentDokumentId(
    @Column(name = "persistent_dokument_id")
    open var persistentDokumentId: UUID,
) : Serializable



