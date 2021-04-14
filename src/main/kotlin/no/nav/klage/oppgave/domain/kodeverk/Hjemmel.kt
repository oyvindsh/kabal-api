package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Hjemmel(val id: Int, val lov: LovKilde, val posisjon: LovPosisjon? = null) {

    FTL(1, LovKilde.FOLKETRYGDLOVEN),
    FTL_9(2, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(9)),
    FTL_9_1(3, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(9, 1)),
    FTL_8_4(4, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(8, 4)),
    FTL_8_5(5, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(8, 5)),
    FTL_8_21(6, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(8, 21)),
    FTL_8_22(7, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(8, 22)),
    FTL_8_35(8, LovKilde.FOLKETRYGDLOVEN, LovPosisjon(8, 35))
    ;

    fun toSearchableString(): String {
        if (posisjon == null) {
            return lov.name
        }
        if (posisjon.paragraf == null) {
            return "${lov.name} ${posisjon.kapittel}"
        }
        return "${lov.name} ${posisjon.kapittel}-${posisjon.paragraf}"
    }

    companion object {
        fun of(id: Int): Hjemmel {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Hjemmel with ${id} exists")
        }

        fun of(lov: LovKilde, posisjon: LovPosisjon?): Hjemmel {
            return values().firstOrNull { it.lov == lov && it.posisjon == posisjon }
                ?: throw IllegalArgumentException("No Hjemmel with lov $lov and posisjon $posisjon exists")
        }
    }

}

data class LovPosisjon(val kapittel: Int, val paragraf: Int? = null)

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
