package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Hjemmel(val id: Int, val lov: LovKilde, val kapittelOgParagraf: KapittelOgParagraf? = null) {

    FTL(1, LovKilde.FOLKETRYGDLOVEN),
    FTL_9(2, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(9)),
    FTL_9_1(3, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(9, 1)),
    FTL_8_4(4, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 4)),
    FTL_8_5(5, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 5)),
    FTL_8_21(6, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 21)),
    FTL_8_22(7, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 22)),
    FTL_8_35(8, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 35))
    ;

    fun toSearchableString(): String {
        if (kapittelOgParagraf == null) {
            return lov.name
        }
        if (kapittelOgParagraf.paragraf == null) {
            return "${lov.name} ${kapittelOgParagraf.kapittel}"
        }
        return "${lov.name} ${kapittelOgParagraf.kapittel}-${kapittelOgParagraf.paragraf}"
    }

    companion object {
        fun of(id: Int): Hjemmel {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Hjemmel with ${id} exists")
        }

        fun of(lov: LovKilde, kapittelOgParagraf: KapittelOgParagraf?): Hjemmel {
            return values().firstOrNull { it.lov == lov && it.kapittelOgParagraf == kapittelOgParagraf }
                ?: throw IllegalArgumentException("No Hjemmel with lov $lov and kapittelOgParagraf $kapittelOgParagraf exists")
        }
    }

}

data class KapittelOgParagraf(val kapittel: Int, val paragraf: Int? = null)

enum class LovKilde {
    FOLKETRYGDLOVEN, FORVALTNINGSLOVEN
}

@Converter
class HjemmelConverter : AttributeConverter<Hjemmel, Int?> {

    override fun convertToDatabaseColumn(entity: Hjemmel?): Int? =
        entity?.let { it.id }

    override fun convertToEntityAttribute(id: Int?): Hjemmel? =
        id?.let { Hjemmel.of(it) }
}
