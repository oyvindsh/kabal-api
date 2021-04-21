package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Type(override val id: Int, override val navn: String, override val beskrivelse: String) : Kode {

    KLAGE(1, "Klage", "Klage"),
    ANKE(2, "Anke", "Anke");

    override fun toString(): String {
        return "Sakstype(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: Int): Type {
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
class TypeConverter : AttributeConverter<Type, Int?> {

    override fun convertToDatabaseColumn(entity: Type?): Int? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: Int?): Type? =
        id?.let { Type.of(it) }
}