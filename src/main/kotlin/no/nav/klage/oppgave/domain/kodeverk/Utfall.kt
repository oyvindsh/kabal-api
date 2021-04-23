package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Utfall(override val id: Int, override val navn: String, override val beskrivelse: String) : Kode {

    TRUKKET(1, "Trukket", "Trukket"),
    RETUR(2, "Retur", "Retur"),
    OPPHEVET(3, "Opphevet", "Opphevet"),
    MEDHOLD(4, "Medhold", "Medhold"),
    DELVIS_MEDHOLD(5, "Delvis medhold", "Delvis medhold"),
    OPPRETTHOLDT(6, "Opprettholdt", "Opprettholdt"),
    UGUNST(7, "Ugunst (Ugyldig)", "Ugunst (Ugyldig)"),
    AVVIST(8, "Avvist", "Avvist");

    override fun toString(): String {
        return "Utfall(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: Int): Utfall {
            return values().firstOrNull { it.id == id }
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
