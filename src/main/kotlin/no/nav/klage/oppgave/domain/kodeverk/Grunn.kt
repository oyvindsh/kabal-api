package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Grunn(val id: Int, val navn: String, val beskrivelse: String?) {

    MANGELFULL_UTREDNING(1, "Mangelfull utredning", null),
    ANDRE_SAKSBEHANDLINGSFEIL(2, "Andre saksbehandlingsfeil", null),
    ENDRET_FAKTUM(3, "Endret faktum", null),
    FEIL_I_BEVISVURDERINGEN(4, "Feil i bevisvurderingen", null),
    FEIL_I_DEN_GENERELLE_LOVTOLKNINGEN(5, "Feil i den generelle lovtolkningen", null),
    FEIL_I_DEN_KONKRETE_RETTSANVENDELSEN(6, "Feil i den konkrete rettsanvendelsen", null);

    override fun toString(): String {
        return "Grunn(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: Int): Grunn {
            return Grunn.values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Grunn with ${id} exists")
        }
    }

}

@Converter
class GrunnConverter : AttributeConverter<Grunn, Int?> {

    override fun convertToDatabaseColumn(entity: Grunn?): Int? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: Int?): Grunn? =
        id?.let { Grunn.of(it) }
}
