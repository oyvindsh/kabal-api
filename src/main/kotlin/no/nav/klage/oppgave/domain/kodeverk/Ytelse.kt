package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Ytelse(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {
    OMS_OMP("1", "Omsorgspenger", "Omsorgspenger"),
    OMS_OLP("2", "Opplæringspenger", "Opplæringspenger"),
    OMS_PSB("3", "Pleiepenger sykt barn", "Pleiepenger sykt barn"),
    OMS_PLS("4", "Pleiepenger i livets sluttfase", "Pleiepenger i livets sluttfase"),
    SYK_SYK("5", "Sykepenger", "Sykepenger")
    ;


    companion object {
        fun of(id: String): Ytelse {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Ytelse with $id exists")
        }
    }
}

@Converter
class YtelseConverter : AttributeConverter<Ytelse, String?> {

    override fun convertToDatabaseColumn(entity: Ytelse?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Ytelse? =
        id?.let { Ytelse.of(it) }
}