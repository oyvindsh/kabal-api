package no.nav.klage.oppgave.domain.klage

import no.nav.klage.kodeverk.hjemmel.LovKilde
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "mottak_hjemmel", schema = "klage")
class MottakHjemmel(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "lov")
    @Enumerated(EnumType.STRING)
    val lov: LovKilde? = null,
    @Column(name = "kapittel")
    val kapittel: Int? = null,
    @Column(name = "paragraf")
    val paragraf: Int? = null,
    @Column(name = "hjemmel_id")
    val hjemmelId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MottakHjemmel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
