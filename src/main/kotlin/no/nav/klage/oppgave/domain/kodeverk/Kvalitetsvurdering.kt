package no.nav.klage.oppgave.domain.kodeverk

import javax.persistence.AttributeConverter
import javax.persistence.Converter


enum class KvalitetsavvikOversendelsesbrev(
    override val id: String,
    override val navn: String,
    override val beskrivelse: String
) : Kode {
    OVERSITTET_KLAGEFRIST_IKKE_KOMMENTERT(
        "1",
        "Oversittet klagefrist er ikke kommentert",
        "Oversittet klagefrist er ikke kommentert"
    ),
    HOVEDINNHOLDET_IKKE_GJENGITT(
        "2",
        "Hovedinnholdet i klagen er ikke gjengitt",
        "Hovedinnholdet i klagen er ikke gjengitt"
    ),
    MANGLER_BEGRUNNELSE(
        "3",
        "Mangler begrunnelse for hvorfor vedtaket opprettholdes/hvorfor klager ikke oppfyller villkår",
        "Mangler begrunnelse for hvorfor vedtaket opprettholdes/hvorfor klager ikke oppfyller villkår"
    ),
    KLAGERS_ANFOERSLER_IKKE_TILSTREKKELIG_KOMMENTERT(
        "4",
        "Klagers anførsler er ikke tilstrekkelig kommentert/imøtegått",
        "Klagers anførsler er ikke tilstrekkelig kommentert/imøtegått"
    ),
    MANGLER_KONKLUSJON("5", "Mangler konklusjon", "Mangler konklusjon");

    override fun toString(): String {
        return "KvalitetsavvikOversendelsesbrev(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): KvalitetsavvikOversendelsesbrev {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No KvalitetsavvikOversendelsesbrev with $id exists")
        }
    }
}

enum class KvalitetsavvikUtredning(
    override val id: String,
    override val navn: String,
    override val beskrivelse: String
) : Kode {
    MANGELFULL_UTREDNING_AV_MEDISINSKE_FORHOLD(
        "1",
        "Mangelfull utredning av medisinske forhold ",
        "Mangelfull utredning av medisinske forhold "
    ),
    MANGELFULL_UTREDNING_AV_ARBEIDSFORHOLD(
        "2",
        "Mangelfull utredning av arbeids- og inntektsforhold",
        "Mangelfull utredning av arbeids- og inntektsforhold"
    ),
    MANGELFULL_UTREDNING_AV_UTENLANDSPROBLEMATIKK(
        "3",
        "Mangelfull utredning av EØS/utlandsproblematikk",
        "Mangelfull utredning av EØS/utlandsproblematikk"
    ),
    MANGELFULL_BRUK_AV_ROL(
        "4",
        "Mangelfull bruk av rådgivende lege",
        "Mangelfull bruk av rådgivende lege"
    ),
    MANGELFULL_UTREDNING_ANDRE_FORHOLD(
        "5",
        "Mangelfull utredning av andre aktuelle forhold i saken",
        "Mangelfull utredning av andre aktuelle forhold i saken"
    );

    override fun toString(): String {
        return "KvalitetsavvikUtredning(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): KvalitetsavvikUtredning {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No KvalitetsavvikUtredning with $id exists")
        }
    }
}

enum class KvalitetsavvikVedtak(
    override val id: String,
    override val navn: String,
    override val beskrivelse: String
) : Kode {
    IKKE_BRUKT_RIKTIGE_HJEMLER(
        "1",
        "Det er ikke brukt riktig hjemmel/er",
        "Det er ikke brukt riktig hjemmel/er"
    ),
    INNHOLDET_I_RETTSREGLENE_IKKE_TILSTREKKELIG_BESKREVET(
        "2",
        "Innholdet i rettsreglene er ikke tilstrekkelig beskrevet",
        "Innholdet i rettsreglene er ikke tilstrekkelig beskrevet"
    ),
    VURDERING_AV_BEVIS_ER_MANGELFULL(
        "3",
        "Vurderingen av faktum/bevisvurderingen er mangelfull",
        "Vurderingen av faktum/bevisvurderingen er mangelfull"
    ),
    BEGRUNNELSE_IKKE_TILSTREKKELIG_KONKRET_OG_INDVIDUELL(
        "4",
        "Begrunnelsen er ikke tilstrekkelig konkret og individuell",
        "Begrunnelsen er ikke tilstrekkelig konkret og individuell"
    ),
    FORMIDLING_IKKE_TYDELIG("5", "Formidlingen er ikke tydelig", "Formidlingen er ikke tydelig");

    override fun toString(): String {
        return "KvalitetsavvikVedtak(id=$id, " +
                "navn=$navn)"
    }

    companion object {
        fun of(id: String): KvalitetsavvikVedtak {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No KvalitetsavvikVedtak with $id exists")
        }
    }
}

@Converter
class KvalitetsavvikOversendelsesbrevConverter : AttributeConverter<KvalitetsavvikOversendelsesbrev, String?> {

    override fun convertToDatabaseColumn(entity: KvalitetsavvikOversendelsesbrev?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): KvalitetsavvikOversendelsesbrev? =
        id?.let { KvalitetsavvikOversendelsesbrev.of(it) }
}

@Converter
class KvalitetsavvikUtredningConverter : AttributeConverter<KvalitetsavvikUtredning, String?> {

    override fun convertToDatabaseColumn(entity: KvalitetsavvikUtredning?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): KvalitetsavvikUtredning? =
        id?.let { KvalitetsavvikUtredning.of(it) }
}

@Converter
class KvalitetsavvikVedtakConverter : AttributeConverter<KvalitetsavvikVedtak, String?> {

    override fun convertToDatabaseColumn(entity: KvalitetsavvikVedtak?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): KvalitetsavvikVedtak? =
        id?.let { KvalitetsavvikVedtak.of(it) }
}