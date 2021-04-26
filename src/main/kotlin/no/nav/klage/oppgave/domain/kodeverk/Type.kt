package no.nav.klage.oppgave.domain.kodeverk

import io.swagger.annotations.ApiModel
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@ApiModel
enum class Type(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {

    KLAGE("1", "Klage", "Klage"),
    ANKE("2", "Anke", "Anke");

    override fun toString(): String {
        return "Type(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): Type {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Type with ${id} exists")
        }

        fun fromNavn(navn: String): Type {
            return values().firstOrNull { it.navn == navn }
                ?: throw IllegalArgumentException("No Type with ${navn} exists")
        }
    }
}

@Converter
class TypeConverter : AttributeConverter<Type, String?> {

    override fun convertToDatabaseColumn(entity: Type?): String? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: String?): Type? =
        id?.let { Type.of(it) }
}
