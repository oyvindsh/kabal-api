package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "vedtak", schema = "klage")
class Vedtak(
    @Id
    val id: Int,
    @Column(name = "enhet")
    val enhet: Int,
    @Column(name = "eoes_id")
    val eoesId: Int,
    @Column(name = "rol_id")
    val rolId: Int,
    @Column(name = "utfall_id")
    val utfallId: Int,
    @Column(name = "grunn_id")
    val grunnId: Int,
    @Column(name = "tilbakemelding_id")
    val tilbakemeldingId: Int?,
    @Column(name = "vedtaksdokument_id")
    val vedtaksdokumentId: Int?,
    @Column(name = "modified")
    val modified: LocalDateTime,
    @Column(name = "created")
    val created: LocalDateTime
) {
    override fun toString(): String {
        return "Vedtak(id=$id, " +
                "modified=$modified, " +
                "created=$created)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vedtak

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
