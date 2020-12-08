package no.nav.klage.oppgave.domain.oppgavekopi

enum class Status {

    OPPRETTET,
    AAPNET,
    UNDER_BEHANDLING,
    FERDIGSTILT,
    FEILREGISTRERT;

    companion object {
        fun getIdFor(status: Status): Long {
            return when (status) {
                OPPRETTET -> 1L
                AAPNET -> 2L
                UNDER_BEHANDLING -> 3L
                FERDIGSTILT -> 4L
                FEILREGISTRERT -> 5L
            }
        }

        fun kategoriForStatus(status: Status): Statuskategori {
            return when (status) {
                Status.AAPNET, Status.OPPRETTET, Status.UNDER_BEHANDLING -> Statuskategori.AAPEN
                Status.FEILREGISTRERT, Status.FERDIGSTILT -> Statuskategori.AVSLUTTET
            }
        }
    }

    fun kategoriForStatus(): Statuskategori {
        return kategoriForStatus(this)
    }

    fun getId(): Long = getIdFor(this)
}
