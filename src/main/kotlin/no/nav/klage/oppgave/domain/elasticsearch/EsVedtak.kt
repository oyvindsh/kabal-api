package no.nav.klage.oppgave.domain.elasticsearch

import org.springframework.data.elasticsearch.annotations.*
import java.time.LocalDateTime

data class EsVedtak(
    @Field(type = FieldType.Keyword)
    val utfall: String?,
    @Field(type = FieldType.Keyword)
    val grunn: String?,
    @MultiField(
        mainField = Field(type = FieldType.Keyword),
        otherFields = [InnerField(type = FieldType.Text, suffix = "text")]
    )
    val hjemler: List<String>,
    @Field(type = FieldType.Keyword)
    val brevmottakerFnr: List<String>,
    @Field(type = FieldType.Keyword)
    val brevmottakerOrgnr: List<String>,
    @Field(type = FieldType.Keyword)
    val journalpostId: String?,
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    val created: LocalDateTime,
    @Field(type = FieldType.Date, format = DateFormat.date_time)
    val modified: LocalDateTime,
)
