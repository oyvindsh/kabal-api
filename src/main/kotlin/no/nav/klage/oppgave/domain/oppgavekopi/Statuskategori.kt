package no.nav.klage.oppgave.domain.oppgavekopi

enum class Statuskategori {
    AAPEN,
    AVSLUTTET;

    fun statuserForKategori(kategori: Statuskategori): List<Status> {
        return when (kategori) {
            Statuskategori.AAPEN -> aapen()
            Statuskategori.AVSLUTTET -> avsluttet()
        }
    }

    fun avsluttet(): List<Status> {
        return listOf(Status.FERDIGSTILT, Status.FEILREGISTRERT)
    }

    fun aapen(): List<Status> {
        return listOf(Status.OPPRETTET, Status.AAPNET, Status.UNDER_BEHANDLING)
    }
}
