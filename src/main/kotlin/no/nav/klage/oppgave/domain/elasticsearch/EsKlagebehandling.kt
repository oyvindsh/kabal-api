package no.nav.klage.oppgave.domain.elasticsearch

import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import org.elasticsearch.index.VersionType
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.elasticsearch.annotations.*
import java.time.LocalDate

@Document(
    indexName = "klagebehandling",
    shards = 3,
    replicas = 2,
    versionType = VersionType.EXTERNAL,
    createIndex = false
)
data class EsKlagebehandling(
    @Id
    val id: String,
    //Må være Long? for å bli Long på JVMen (isf long), og det krever Spring DataES..
    @Version
    val versjon: Long?,
    @Field(type = FieldType.Keyword)
    val journalpostId: List<String> = listOf(),
    @Field(type = FieldType.Keyword)
    val saksreferanse: String? = null,
    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [InnerField(type = FieldType.Text, suffix = "text")]
    )
    val tildeltEnhet: String?,
    @Field(type = FieldType.Keyword)
    val tema: Tema,
    @Field(type = FieldType.Keyword)
    val sakstype: Sakstype,
    @Field(type = FieldType.Keyword)
    val tildeltSaksbehandlerident: String? = null,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val innsendt: LocalDate? = null,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val mottattFoersteinstans: LocalDate? = null,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val mottattKlageinstans: LocalDate?,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val frist: LocalDate?,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val startet: LocalDate? = null,
    @Field(type = FieldType.Date, format = DateFormat.date)
    val avsluttet: LocalDate? = null,
    @Field(type = FieldType.Keyword)
    val hjemler: List<Int>? = null,
    @Field(type = FieldType.Keyword)
    val foedselsnummer: String? = null,
    @Field(type = FieldType.Text)
    val navn: String? = null,
    @Field(type = FieldType.Boolean)
    val egenAnsatt: Boolean = false,
    @Field(type = FieldType.Boolean)
    val fortrolig: Boolean = false,
    @Field(type = FieldType.Boolean)
    val strengtFortrolig: Boolean = false
)