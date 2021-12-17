package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "mottak_hjemmel", schema = "klage")
class MottakHjemmel(
    @Id
    val id: UUID = UUID.randomUUID(),
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
