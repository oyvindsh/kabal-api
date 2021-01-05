package no.nav.klage.oppgave.domain.klage

import java.time.LocalDate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "klage", schema = "klage")
class Klage(

    @Id
    var id: UUID = UUID.randomUUID(),
    @Column(name = "foedselsnummer")
    val foedselsnummer: String,
    @Column(name = "dato_mottatt_fra_foersteinstans")
    val datoMottattFraFoersteinstans: LocalDate,
    @Column(name = "frist")
    val frist: LocalDate,
    @Column(name = "tildelt_saksbehandlerident")
    val tildeltSaksbehandlerident: String? = null
) {

    override fun toString(): String {
        return "Klage(id=$id, foedselsnummer='$foedselsnummer', " +
                "datoMottattFraFoersteinstans=$datoMottattFraFoersteinstans, frist=$frist, " +
                "tildeltSaksbehandlerident=$tildeltSaksbehandlerident)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Klage

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}