package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Lov(val id: Int, val navn: String, val beskrivelse: String) {

    FOLKETRYGDLOVEN(1, "Folketrygdloven", "Lov om folketrygd");

    override fun toString(): String {
        return "Lov(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: Int): Lov {
            return Lov.values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Lov with ${id} exists")
        }
    }
}

@Converter
class LovConverter : AttributeConverter<Lov, Int?> {

    override fun convertToDatabaseColumn(entity: Lov?): Int? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: Int?): Lov? =
        id?.let { Lov.of(it) }
}