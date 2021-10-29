package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatus
import no.nav.klage.oppgave.domain.kodeverk.UtsendingStatusConverter
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "kafka_dvh_event", schema = "klage")
class KafkaDVHEvent(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "klagebehandling_id")
    var klagebehandlingId: UUID,
    @Column(name = "kilde_referanse")
    var kildeReferanse: String,
    @Column(name = "kilde")
    var kilde: String,
    @Column(name = "status_id")
    @Convert(converter = UtsendingStatusConverter::class)
    var status: UtsendingStatus,
    @Column(name = "json_payload")
    var jsonPayload: String,
    @Column(name = "error_message")
    var errorMessage: String? = null
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KafkaDVHEvent

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "KafkaDVHEvent(id=$id, klagebehandlingId=$klagebehandlingId, kildeReferanse='$kildeReferanse', kilde='$kilde', status=$status, jsonPayload='$jsonPayload', errorMessage=$errorMessage)"
    }

}
