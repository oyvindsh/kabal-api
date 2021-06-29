package no.nav.klage.oppgave.api.view

data class PersonSoekInput(
    val soekString: String,
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val sortering: Sortering? = Sortering.FRIST,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null
) {

    fun isFnrSoek() = soekString.isNumeric()

    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Sortering {
        FRIST, MOTTATT
    }

    enum class Projeksjon {
        UTVIDET
    }

    private fun String.isNumeric() = toIntOrNull() != null
}
