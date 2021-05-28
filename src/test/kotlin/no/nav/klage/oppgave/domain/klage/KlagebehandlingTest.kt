package no.nav.klage.oppgave.domain.klage

import no.nav.klage.oppgave.domain.kodeverk.Fagsystem
import no.nav.klage.oppgave.domain.kodeverk.PartIdType
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class KlagebehandlingTest {

    @Test
    fun `status IKKE_TILDELT`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.IKKE_TILDELT)
    }

    @Test
    fun `status IKKE_TILDELT etter tidligere tildeling`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE,
            tildeling = Tildeling(saksbehandlerident = null, tidspunkt = LocalDateTime.now())
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.IKKE_TILDELT)
    }

    @Test
    fun `status TILDELT`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE,
            tildeling = Tildeling(saksbehandlerident = "abc", tidspunkt = LocalDateTime.now())
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.TILDELT)
    }

    @Test
    fun `status SENDT_TIL_MEDUNDERSKRIVER`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE,
            medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now())
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.SENDT_TIL_MEDUNDERSKRIVER)
    }

    @Test
    fun `status status TILDELT når medunderskriver er fjernet`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE,
            tildeling = Tildeling(saksbehandlerident = "abc", tidspunkt = LocalDateTime.now()),
            medunderskriver = MedunderskriverTildeling(null, LocalDateTime.now())
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.TILDELT)
    }

    @Test
    fun `status GODKJENT_AV_MEDUNDERSKRIVER`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE,
            medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now()),
            avsluttetAvSaksbehandler = LocalDateTime.now()
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.GODKJENT_AV_MEDUNDERSKRIVER)
    }

    @Test
    fun `status FULLFOERT`() {
        val klagebehandling = Klagebehandling(
            kildesystem = Fagsystem.AO01,
            klager = Klager(PartId(PartIdType.PERSON, "123")),
            sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, "123"), false),
            mottakId = UUID.randomUUID(),
            mottattKlageinstans = LocalDateTime.now(),
            tema = Tema.AAP,
            type = Type.KLAGE,
            medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now()),
            avsluttet = LocalDateTime.now()
        )
        assertThat(klagebehandling.getStatus()).isEqualTo(Klagebehandling.Status.FULLFOERT)
    }

}