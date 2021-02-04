package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Utfall(val id: Int, val navn: String, val beskrivelse: String?) {

    TRUKKET(1, "Trukket", null),
    RETUR(2, "Retur", null),
    OPPHEVET(3, "Opphevet", null),
    MEDHOLD(4, "Medhold", null),
    DELVIS_MEDHOLD(5, "Delvis medhold", null),
    OPPRETTHOLDT(6, "Opprettholdt", null),
    UGUNST(7, "Ugunst (Ugyldig)", null),
    AVVIST(8, "Avvist", null);

    override fun toString(): String {
        return "Utfall(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: Int): Utfall {
            return Utfall.values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Utfall with ${id} exists")
        }
    }

}

@Converter
class UtfallConverter : AttributeConverter<Utfall, Int?> {

    override fun convertToDatabaseColumn(entity: Utfall?): Int? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: Int?): Utfall? =
        id?.let { Utfall.of(it) }
}
