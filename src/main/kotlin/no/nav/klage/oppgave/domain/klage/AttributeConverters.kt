package no.nav.klage.oppgave.domain.klage

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import no.nav.klage.kodeverk.*
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.Registreringshjemmel

@Converter
class BrevmottakertypeConverter : AttributeConverter<Brevmottakertype, String?> {

    override fun convertToDatabaseColumn(entity: Brevmottakertype?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Brevmottakertype? =
        id?.let { Brevmottakertype.of(it) }
}

@Converter
class DokumentTypeConverter : AttributeConverter<DokumentType, String?> {

    override fun convertToDatabaseColumn(entity: DokumentType?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): DokumentType? =
        id?.let { DokumentType.of(it) }
}

@Converter
class FagsystemConverter : AttributeConverter<Fagsystem, String?> {

    override fun convertToDatabaseColumn(entity: Fagsystem?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Fagsystem? =
        id?.let { Fagsystem.of(it) }
}

@Converter
class MedunderskriverflytConverter : AttributeConverter<MedunderskriverFlyt, String?> {

    override fun convertToDatabaseColumn(entity: MedunderskriverFlyt?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): MedunderskriverFlyt? =
        id?.let { MedunderskriverFlyt.of(it) }
}

@Converter
class ROLStateConverter : AttributeConverter<ROLState, String?> {

    override fun convertToDatabaseColumn(entity: ROLState?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): ROLState? =
        id?.let { ROLState.of(it) }
}

@Converter
class PartIdTypeConverter : AttributeConverter<PartIdType, String?> {

    override fun convertToDatabaseColumn(entity: PartIdType?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): PartIdType? =
        id?.let { PartIdType.of(it) }
}

@Converter
class TemaConverter : AttributeConverter<Tema, String?> {

    override fun convertToDatabaseColumn(entity: Tema?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Tema? =
        id?.let { Tema.of(it) }
}

@Converter
class TypeConverter : AttributeConverter<Type, String?> {

    override fun convertToDatabaseColumn(entity: Type?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Type? =
        id?.let { Type.of(it) }
}

@Converter
class UtfallConverter : AttributeConverter<Utfall, String?> {

    override fun convertToDatabaseColumn(entity: Utfall?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Utfall? =
        id?.let { Utfall.of(it) }
}

@Converter
class YtelseConverter : AttributeConverter<Ytelse, String?> {

    override fun convertToDatabaseColumn(entity: Ytelse?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Ytelse? =
        id?.let { Ytelse.of(it) }
}

@Converter
class HjemmelConverter : AttributeConverter<Hjemmel, String?> {

    override fun convertToDatabaseColumn(entity: Hjemmel?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Hjemmel? =
        id?.let { Hjemmel.of(it) }
}

@Converter
class RegistreringshjemmelConverter : AttributeConverter<Registreringshjemmel, String?> {

    override fun convertToDatabaseColumn(entity: Registreringshjemmel?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Registreringshjemmel? =
        id?.let { Registreringshjemmel.of(it) }
}