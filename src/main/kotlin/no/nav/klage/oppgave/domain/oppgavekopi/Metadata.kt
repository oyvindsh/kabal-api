package no.nav.klage.oppgave.domain.oppgavekopi

import javax.persistence.*

@Entity
@Table(name = "metadata", schema = "oppgave")
data class Metadata(
    @Id
    @Column(name = "id")
    @SequenceGenerator(
        name = "metadata_seq",
        sequenceName = "oppgave.metadata_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "metadata_seq")
    var id: Long? = null,
    @Column(name = "nokkel")
    var noekkel: MetadataNoekkel,
    @Column(name = "verdi")
    var verdi: String
)