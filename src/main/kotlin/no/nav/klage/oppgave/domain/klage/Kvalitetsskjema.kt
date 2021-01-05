package no.nav.klage.oppgave.domain.klage

import java.util.*
import javax.persistence.*

@Entity
@Table(name = "kvalitetsskjema", schema = "klage")
class Kvalitetsskjema(

    @Id
    @Column(name = "klage_id")
    var id: UUID,
    @Column(name = "enhet")
    val enhet: Int,
    @Column(name = "sakstype")
    val sakstype: String,
    @Column(name = "eoes")
    val eoes: Int? = null,
    @Column(name = "rol")
    val rol: Int? = null,
    @Column(name = "utfall")
    val utfall: String,
    @Column(name = "omgjoeringsgrunn")
    val omgjoeringsgrunn: Int?,
    @Column(name = "motta_tilbakemelding")
    val mottaTilbakemelding: Boolean = false,
    @Column(name = "tilbakemelding")
    val tilbakemelding: String? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @MapsId
    @JoinColumn(name = "klage_id")
    var klage: Klage
) {

    override fun toString(): String {
        return "Kvalitetsskjema(id=$id, enhet=$enhet, sakstype='$sakstype', eoes=$eoes, rol=$rol, utfall='$utfall', " +
                "omgjoeringsgrunn=$omgjoeringsgrunn, mottaTilbakemelding=$mottaTilbakemelding, " +
                "tilbakemelding=$tilbakemelding)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Kvalitetsskjema

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}