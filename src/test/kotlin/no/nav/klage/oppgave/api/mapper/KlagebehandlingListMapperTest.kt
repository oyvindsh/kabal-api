package no.nav.klage.oppgave.api.mapper

import no.nav.klage.oppgave.api.view.PersonSoekPersonView
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class KlagebehandlingListMapperTest {

    val fnr1 = "01011012345"
    val fnr2 = "02022012345"

    val klagebehandling1 = EsKlagebehandling(
        id = "1001L",
        versjon = 1L,
        sakenGjelderFnr = fnr1,
        tildeltEnhet = "4219",
        tema = Tema.OMS.id,
        type = Type.KLAGE.id,
        innsendt = LocalDate.of(2019, 10, 1),
        mottattFoersteinstans = LocalDate.of(2019, 11, 1),
        mottattKlageinstans = LocalDateTime.of(2019, 12, 1, 0, 0),
        frist = LocalDate.of(2020, 12, 1),
        hjemler = listOf(),
        created = LocalDateTime.now(),
        modified = LocalDateTime.now(),
        kilde = "K9",
        temaNavn = Tema.OMS.name,
        typeNavn = Type.KLAGE.name,
        status = EsKlagebehandling.Status.IKKE_TILDELT
    )
    val klagebehandling2 =
        EsKlagebehandling(
            id = "1002L",
            versjon = 1L,
            sakenGjelderFnr = fnr1,
            tildeltEnhet = "4219",
            tema = Tema.SYK.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2018, 10, 1),
            mottattFoersteinstans = LocalDate.of(2018, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2018, 12, 1, 0, 0),
            frist = LocalDate.of(2019, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            temaNavn = Tema.SYK.name,
            typeNavn = Type.KLAGE.name,
            status = EsKlagebehandling.Status.IKKE_TILDELT
        )
    val klagebehandling3 =
        EsKlagebehandling(
            id = "1003L",
            versjon = 1L,
            sakenGjelderFnr = fnr2,
            tildeltEnhet = "4219",
            tema = Tema.SYK.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
            innsendt = LocalDate.of(2018, 10, 1),
            mottattFoersteinstans = LocalDate.of(2018, 11, 1),
            mottattKlageinstans = LocalDateTime.of(2018, 12, 1, 0, 0),
            frist = LocalDate.of(2019, 12, 1),
            hjemler = listOf(),
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            kilde = "K9",
            temaNavn = Tema.SYK.name,
            typeNavn = Type.KLAGE.name,
            status = EsKlagebehandling.Status.IKKE_TILDELT
        )
}
