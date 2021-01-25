package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "ident", schema = "oppgave")
class Ident(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "ident_seq",
        sequenceName = "oppgave.ident_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ident_seq")
    val id: Long? = null,
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    val identType: IdentType,
    @Column(name = "verdi")
    val verdi: String,
    @Column(name = "folkeregisterident")
    val folkeregisterident: String? = null,
    @Column(name = "registrert_dato")
    val registrertDato: LocalDate? = null


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Ident

        if (identType != other.identType) return false
        if (verdi != other.verdi) return false
        if (folkeregisterident != other.folkeregisterident) return false
        if (registrertDato != other.registrertDato) return false

        return true
    }

    override fun hashCode(): Int {
        var result = identType.hashCode()
        result = 31 * result + verdi.hashCode()
        result = 31 * result + (folkeregisterident?.hashCode() ?: 0)
        result = 31 * result + (registrertDato?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Ident(id=$id, identType=$identType, verdi='$verdi', folkeregisterident=$folkeregisterident, registrertDato=$registrertDato)"
    }
}
