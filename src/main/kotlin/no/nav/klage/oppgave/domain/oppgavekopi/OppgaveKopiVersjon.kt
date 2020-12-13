package no.nav.klage.oppgave.domain.oppgavekopi

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "oppgaveversjon", schema = "oppgave")
@IdClass(OppgaveKopiVersjonId::class)
data class OppgaveKopiVersjon(

    @Id
    @Column(name = "id")
    var id: Long,
    @Id
    @Column(name = "versjon")
    var versjon: Int,
    @Column(name = "journalpostid")
    var journalpostId: String? = null,
    @Column(name = "saksreferanse")
    var saksreferanse: String? = null,
    @Column(name = "mappe_id")
    var mappeId: Long? = null,
    @Column(name = "status_id")
    @Convert(converter = StatusConverter::class)
    var status: Status,
    @Column(name = "tildelt_enhetsnr")
    var tildeltEnhetsnr: String,
    @Column(name = "opprettet_av_enhetsnr")
    var opprettetAvEnhetsnr: String? = null,
    @Column(name = "endret_av_enhetsnr")
    var endretAvEnhetsnr: String? = null,
    @Column(name = "tema")
    var tema: String,
    @Column(name = "temagruppe")
    var temagruppe: String? = null,
    @Column(name = "behandlingstema")
    var behandlingstema: String? = null,
    @Column(name = "oppgavetype")
    var oppgavetype: String,
    @Column(name = "behandlingstype")
    var behandlingstype: String? = null,
    @Column(name = "prioritet")
    @Enumerated(EnumType.STRING)
    var prioritet: Prioritet,
    @Column(name = "tilordnet_ressurs")
    var tilordnetRessurs: String? = null,
    @Column(name = "beskrivelse")
    var beskrivelse: String? = null,
    @Column(name = "frist_ferdigstillelse")
    var fristFerdigstillelse: LocalDate,
    @Column(name = "aktiv_dato")
    var aktivDato: LocalDate,
    @Column(name = "opprettet_av")
    var opprettetAv: String,
    @Column(name = "endret_av")
    var endretAv: String? = null,
    @Column(name = "opprettet_tidspunkt")
    var opprettetTidspunkt: LocalDateTime,
    @Column(name = "endret_tidspunkt")
    var endretTidspunkt: LocalDateTime? = null,
    @Column(name = "ferdigstilt_tidspunkt")
    var ferdigstiltTidspunkt: LocalDateTime? = null,
    @Column(name = "behandles_av_applikasjon")
    var behandlesAvApplikasjon: String? = null,
    @Column(name = "journalpostkilde")
    var journalpostkilde: String? = null,
    @OneToOne(cascade = [CascadeType.ALL])
    @JoinColumn(name = "ident_id", referencedColumnName = "id")
    var ident: VersjonIdent? = null,
    @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumns(
        JoinColumn(
            name = "oppgave_id",
            referencedColumnName = "id",
            nullable = false
        ),
        JoinColumn(
            name = "oppgave_versjon",
            referencedColumnName = "versjon",
            nullable = false
        )
    )
    var metadata: List<VersjonMetadata> = mutableListOf()
) {
    fun statuskategori(): Statuskategori = status.kategoriForStatus()

    fun getMetadataAsMap(): Map<MetadataNoekkel, String> {
        return metadata.map { it.noekkel to it.verdi }.toMap()
    }

    fun setMetadataAsMap(map: Map<MetadataNoekkel, String>) {
        metadata = map.map { VersjonMetadata(noekkel = it.key, verdi = it.value) }
    }
}