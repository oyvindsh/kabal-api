package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Sakstype(val id: String, val navn: String, val beskrivelse: String?) {

    KLAGE("ae0058", "Klage", null),
    ANKE("ae0046", "Anke", null),
    GJENOPPTAK("ae0047", "Gjenopptak", null),
    REVURDERING("ae0028", "Revurdering", null),
    FEILUTBETALING("ae0161", "Feilutbetaling", null);

    override fun toString(): String {
        return "Sakstype(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): Sakstype {
            return Sakstype.values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Sakstype with ${id} exists")
        }
    }
}

@Converter
class SakstypeConverter : AttributeConverter<Sakstype, String?> {

    override fun convertToDatabaseColumn(entity: Sakstype?): String? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: String?): Sakstype? =
        id?.let { Sakstype.of(it) }
}