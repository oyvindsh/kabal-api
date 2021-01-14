package no.nav.klage.oppgave.clients.pdl.kafka

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalDateTime

data class PdlDokument(
    val hentPerson: PersonDto,
    val hentIdenter: HentIdenterDto
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PersonDto(
    val adressebeskyttelse: List<AdressebeskyttelseDto>,
    val kjoenn: List<KjoennDto?>,
    val navn: List<NavnDto>
)

data class AdressebeskyttelseDto(
    val gradering: GraderingDto,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto
)

enum class GraderingDto {
    STRENGT_FORTROLIG_UTLAND, STRENGT_FORTROLIG, FORTROLIG, UGRADERT
}

data class FolkeregistermetadataDto(
    val ajourholdstidspunkt: LocalDateTime?,
    val gyldighetstidspunkt: LocalDateTime?,
    val opphoerstidspunkt: LocalDateTime?,
    val kilde: String?,
    val aarsak: String?,
    val sekvens: Int?
)

data class HentIdenterDto(
    val identer: List<IdentDetaljDto>
)

data class IdentDetaljDto(
    val ident: String,
    val historisk: Boolean,
    val gruppe: IdentGruppeDto,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto?
)

enum class IdentGruppeDto {
    AKTORID, FOLKEREGISTERIDENT, NPID
}

data class KjoennDto(
    val kjoenn: KjoennTypeDto?,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto
)

enum class KjoennTypeDto {
    MANN, KVINNE, UKJENT
}

data class MetadataDto(
    val opplysningsId: String?,
    val master: String,
    val endringer: List<EndringDto>,
    val historisk: Boolean
)

data class EndringDto(
    val type: String,
    val registrert: LocalDateTime,
    val registrertAv: String,
    val systemkilde: String,
    val kilde: String
)

data class NavnDto(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String,
    val forkortetNavn: String?,
    val originaltNavn: OriginaltNavn?,
    val gyldigFraOgMed: LocalDate?,
    val folkeregistermetadata: FolkeregistermetadataDto?,
    val metadata: MetadataDto
)

data class OriginaltNavn(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?
)