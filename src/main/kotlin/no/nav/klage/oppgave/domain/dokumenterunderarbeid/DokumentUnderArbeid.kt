package no.nav.klage.oppgave.domain.dokumenterunderarbeid

import org.hibernate.annotations.DynamicUpdate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "dokument_under_arbeid", schema = "klage")
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@DynamicUpdate
abstract class DokumentUnderArbeid(
    @Id
    var id: UUID,
    //Innfører denne fordi vi trenger noe som er persistent selv når et dokument bytter fra å være et hoveddokument til å bli et vedlegg eller motsatt, og å bruke id skaper litt krøll i persistence contexten..
    @Column(name = "dokument_id")
    var dokumentId: UUID,
    @Column(name = "mellomlager_id")
    var mellomlagerId: UUID,
    @Column(name = "opplastet")
    var opplastet: LocalDateTime,
    @Column(name = "size")
    var size: Long,
    @Column(name = "name")
    var name: String,
    @Column(name = "smarteditor_id")
    var smartEditorId: UUID? = null,
    @Column(name = "behandling_id")
    var behandlingId: UUID,
    @Column(name = "dokument_type")
    @Convert(converter = DokumentTypeConverter::class)
    var dokumentType: DokumentType,
    @Column(name = "created")
    var created: LocalDateTime = LocalDateTime.now(),
    @Column(name = "modified")
    var modified: LocalDateTime = LocalDateTime.now(),
    @Column(name = "markert_ferdig")
    var markertFerdig: LocalDateTime? = null,
    @Column(name = "ferdigstilt")
    var ferdigstilt: LocalDateTime? = null,

    ) {
    companion object {
        const val HOVED_DOKUMENT = "hoved"
        const val VEDLEGG = "vedlegg"
    }

    fun ferdigstillHvisIkkeAlleredeFerdigstilt(tidspunkt: LocalDateTime) {
        if (ferdigstilt != null) {
            ferdigstilt = tidspunkt
            modified = tidspunkt
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DokumentUnderArbeid) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

}


