package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "document_to_merge", schema = "klage")
class DocumentToMerge(
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "reference_id")
    val referenceId: UUID,
    @Column(name = "journalpost_id")
    val journalpostId: String,
    @Column(name = "dokument_info_id")
    val dokumentInfoId: String,
    @Column(name = "created")
    val created: LocalDateTime,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DocumentToMerge

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "DocumentToMerge(id=$id, referenceId=$referenceId, journalpostId='$journalpostId', dokumentInfoId='$dokumentInfoId', created=$created)"
    }

}