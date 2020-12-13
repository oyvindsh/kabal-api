package no.nav.klage.oppgave.domain.oppgavekopi

import javax.persistence.*

@Entity
@Table(name = "versjonmetadata", schema = "oppgave")
data class VersjonMetadata(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "versjonmetadata_seq",
        sequenceName = "oppgave.versjonmetadata_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "versjonmetadata_seq")
    var id: Long? = null,
    @Column(name = "nokkel")
    var noekkel: MetadataNoekkel,
    @Column(name = "verdi")
    var verdi: String
)