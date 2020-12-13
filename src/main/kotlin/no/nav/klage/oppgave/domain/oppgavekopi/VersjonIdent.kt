package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(name = "versjonident", schema = "oppgave")
data class VersjonIdent(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "versjonident_seq",
        sequenceName = "oppgave.versjonident_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "versjonident_seq")
    var id: Long?,
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    var identType: IdentType,
    @Column(name = "verdi")
    var verdi: String,
    @Column(name = "folkeregisterident")
    var folkeregisterident: String? = null,
    @Column(name = "registrert_dato")
    var registrertDato: LocalDate? = null
)
