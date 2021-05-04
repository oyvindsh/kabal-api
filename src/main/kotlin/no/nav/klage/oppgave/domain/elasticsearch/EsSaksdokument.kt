package no.nav.klage.oppgave.domain.elasticsearch

import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType

data class EsSaksdokument(
    @Field(type = FieldType.Keyword)
    val journalpostId: String,
    @Field(type = FieldType.Keyword)
    val dokumentInfoId: String
)

