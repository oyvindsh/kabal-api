package no.nav.klage.oppgave.api.view

//FIXME remove when not in use
data class PersonSoekInput(
    val fnr: String,
    val soekString: String?,
    val rekkefoelge: Rekkefoelge? = Rekkefoelge.STIGENDE,
    val sortering: Sortering? = Sortering.FRIST,
    val start: Int,
    val antall: Int,
    val projeksjon: Projeksjon? = null
) {
    enum class Rekkefoelge {
        STIGENDE, SYNKENDE
    }

    enum class Sortering {
        FRIST, MOTTATT
    }

    enum class Projeksjon {
        UTVIDET
    }
}
