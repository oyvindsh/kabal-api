package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Hjemmel(
    override val id: Int,
    val lov: LovKilde,
    val kapittelOgParagraf: KapittelOgParagraf? = null,
    override val navn: String,
    override val beskrivelse: String
) : Kode {

    FTL(1000, LovKilde.FOLKETRYGDLOVEN, null, "FTL", "Folketrygdloven"),

    //id = lov+kapittel+paragraf. FTL = 1000, kapittel=008, paragraf = 02
    FTL_8_2(100000802, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 2), "FTL 8-2", "Folketrygdloven §8-2"),
    FTL_8_3(100000803, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 3), "FTL 8-3", "Folketrygdloven §8-3"),
    FTL_8_4(100000804, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 4), "FTL 8-4", "Folketrygdloven §8-4"),
    FTL_8_7(100000807, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 7), "FTL 8-7", "Folketrygdloven §8-7"),
    FTL_8_8(100000808, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 8), "FTL 8-8", "Folketrygdloven §8-8"),
    FTL_8_12(100000809, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 12), "FTL 8-12", "Folketrygdloven §8-12"),
    FTL_8_13(100000809, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 13), "FTL 8-13", "Folketrygdloven §8-13"),
    FTL_8_15(100000809, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 15), "FTL 8-15", "Folketrygdloven §8-15"),
    FTL_8_20(100000821, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 20), "FTL 8-20", "Folketrygdloven §8-20"),
    FTL_8_28(100000828, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 28), "FTL 8-28", "Folketrygdloven §8-28"),
    FTL_8_29(100000829, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 29), "FTL 8-29", "Folketrygdloven §8-29"),
    FTL_8_30(100000830, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 30), "FTL 8-30", "Folketrygdloven §8-30"),
    FTL_8_34(100000834, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 34), "FTL 8-34", "Folketrygdloven §8-34"),
    FTL_8_35(100000835, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 35), "FTL 8-35", "Folketrygdloven §8-35"),
    FTL_8_36(100000836, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 36), "FTL 8-36", "Folketrygdloven §8-36"),
    FTL_8_37(100000837, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 37), "FTL 8-37", "Folketrygdloven §8-37"),
    FTL_8_38(100000838, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 38), "FTL 8-38", "Folketrygdloven §8-38"),
    FTL_8_39(100000839, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 39), "FTL 8-39", "Folketrygdloven §8-39"),
    FTL_8_40(100000840, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 40), "FTL 8-40", "Folketrygdloven §8-40"),
    FTL_8_41(100000841, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 41), "FTL 8-41", "Folketrygdloven §8-41"),
    FTL_8_42(100000842, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 42), "FTL 8-42", "Folketrygdloven §8-42"),
    FTL_8_43(100000843, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 43), "FTL 8-43", "Folketrygdloven §8-43"),
    FTL_8_47(100000847, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 47), "FTL 8-47", "Folketrygdloven §8-47"),
    FTL_8_49(100000849, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(8, 49), "FTL 8-49", "Folketrygdloven §8-49"),
    FTL_22_3(100000849, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(22, 3), "FTL 22-3", "Folketrygdloven §22-3"),
    FTL_22_12(100000849, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(22, 12), "FTL 22-12", "Folketrygdloven §22-12"),
    FTL_9(1000009, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(9), "FTL 9", "Folketrygdloven §9"),
    FTL_9_1(100000901, LovKilde.FOLKETRYGDLOVEN, KapittelOgParagraf(9, 1), "FTL 9-1", "Folketrygdloven §9-1"),
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
