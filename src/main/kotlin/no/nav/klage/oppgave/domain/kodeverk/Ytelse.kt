package no.nav.klage.oppgave.domain.kodeverk

import org.springframework.core.env.Environment
import java.util.*
import javax.persistence.AttributeConverter
import javax.persistence.Converter

enum class Ytelse(override val id: String, override val navn: String, override val beskrivelse: String) : Kode {
    OMS_OMP("1", "Omsorgspenger", "Omsorgspenger"),
    OMS_OLP("2", "Opplæringspenger", "Opplæringspenger"),
    OMS_PSB("3", "Pleiepenger sykt barn", "Pleiepenger sykt barn"),
    OMS_PLS("4", "Pleiepenger i livets sluttfase", "Pleiepenger i livets sluttfase"),
    SYK_SYK("5", "Sykepenger", "Sykepenger")
    ;

    companion object {
        fun of(id: String): Ytelse {
            return values().firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("No Ytelse with id $id exists")
        }
    }

    fun toTema(): Tema {
        return when(this) {
            OMS_OMP, OMS_OLP, OMS_PSB, OMS_PLS -> Tema.OMS
            SYK_SYK -> Tema.SYK
        }
    }
}

val ytelserPerEnhet = mapOf(
    "4203" to listOf(Ytelse.SYK_SYK),
    "4205" to listOf(Ytelse.SYK_SYK),
    "4207" to listOf(),
    "4214" to listOf(Ytelse.SYK_SYK),
    "4219" to listOf(Ytelse.OMS_OMP, Ytelse.OMS_PLS, Ytelse.OMS_PSB, Ytelse.OMS_OLP),
    "4250" to listOf(),
)

val enheterPerYtelse = mapOf(
    Ytelse.SYK_SYK to listOf("4203", "4205", "4214"),
    Ytelse.OMS_OMP to listOf("4219"),
    Ytelse.OMS_PLS to listOf("4219"),
    Ytelse.OMS_PSB to listOf("4219"),
    Ytelse.OMS_OLP to listOf("4219"),
)

object LovligeYtelser {
    private val lovligeYtelserIProdGcp = EnumSet.of(Ytelse.OMS_OMP, Ytelse.OMS_OLP, Ytelse.OMS_PSB, Ytelse.OMS_PLS)
    private val lovligeYtelserIDevGcp = EnumSet.of(Ytelse.OMS_OMP, Ytelse.OMS_OLP, Ytelse.OMS_PSB, Ytelse.OMS_PLS, Ytelse.SYK_SYK)

    fun lovligeYtelser(environment: Environment): EnumSet<Ytelse> = if (environment.activeProfiles.contains("prod-gcp")) {
        lovligeYtelserIProdGcp
    } else {
        lovligeYtelserIDevGcp
    }
}

@Converter
class YtelseConverter : AttributeConverter<Ytelse, String?> {

    override fun convertToDatabaseColumn(entity: Ytelse?): String? =
        entity?.id

    override fun convertToEntityAttribute(id: String?): Ytelse? =
        id?.let { Ytelse.of(it) }
}