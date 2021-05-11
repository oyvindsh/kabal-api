package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Fagsystem(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {
    FS36("1", "FS36", "Vedtaksløsning Foreldrepenger"),
    FS39("2", "FS39", "Saksbehandling for Folketrygdloven kapittel 9"),
    AO01("3", "AO01", "Arena"); // Blir satt av Dolly

    companion object {
        fun of(id: String): Fagsystem {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Fagsystem with $id exists")
        }

        fun fromNavn(navn: String): Fagsystem {
            return values().firstOrNull { it.navn == navn }
                ?: throw IllegalArgumentException("No Fagsystem with $navn exists")
        }
    }
}

@Converter
class FagsystemConverter : AttributeConverter<Fagsystem, String?> {

    override fun convertToDatabaseColumn(entity: Fagsystem?): String? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: String?): Fagsystem? =
        id?.let { Fagsystem.of(it) }
}