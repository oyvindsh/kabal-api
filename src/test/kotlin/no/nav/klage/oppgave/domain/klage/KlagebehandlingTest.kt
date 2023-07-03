package no.nav.klage.oppgave.domain.klage

import no.nav.klage.kodeverk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

internal class KlagebehandlingTest {

    private val vedtakId = UUID.randomUUID()
    private val fnr = "12345678910"
    private val fnr2 = "22345678910"
    private val fnr3 = "32345678910"
    private val enhet = "ENHET"

    @Nested
    inner class Status {
        @Test
        fun `status IKKE_TILDELT`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.IKKE_TILDELT)
        }

        @Test
        fun `status IKKE_TILDELT etter tidligere tildeling`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                tildeling = Tildeling(saksbehandlerident = null, tidspunkt = LocalDateTime.now()),
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.IKKE_TILDELT)
        }

        @Test
        fun `status TILDELT`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                tildeling = Tildeling(saksbehandlerident = "abc", tidspunkt = LocalDateTime.now()),
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.TILDELT)
        }

        @Test
        fun `status SENDT_TIL_MEDUNDERSKRIVER`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now()),
                medunderskriverFlyt = MedunderskriverFlyt.OVERSENDT_TIL_MEDUNDERSKRIVER,
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.SENDT_TIL_MEDUNDERSKRIVER)
        }

        @Test
        fun `status RETURNERT_TIL_SAKSBEHANDLER`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now()),
                medunderskriverFlyt = MedunderskriverFlyt.RETURNERT_TIL_SAKSBEHANDLER,
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.RETURNERT_TIL_SAKSBEHANDLER)
        }

        @Test
        fun `status MEDUNDERSKRIVER_VALGT`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now()),
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.MEDUNDERSKRIVER_VALGT)
        }

        @Test
        fun `status TILDELT når medunderskriver er fjernet`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                tildeling = Tildeling(saksbehandlerident = "abc", tidspunkt = LocalDateTime.now()),
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                medunderskriver = MedunderskriverTildeling(null, LocalDateTime.now()),
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.TILDELT)
        }

        @Test
        fun `status FULLFOERT`() {
            val klagebehandling = Klagebehandling(
                fagsystem = Fagsystem.AO01,
                fagsakId = "123",
                kildeReferanse = "abc",
                klager = Klager(PartId(PartIdType.PERSON, fnr)),
                sakenGjelder = SakenGjelder(PartId(PartIdType.PERSON, fnr), false),
                mottakId = UUID.randomUUID(),
                mottattKlageinstans = LocalDateTime.now(),
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                mottattVedtaksinstans = LocalDate.now(),
                avsenderEnhetFoersteinstans = enhet,
                medunderskriver = MedunderskriverTildeling("abc123", LocalDateTime.now()),
                avsluttet = LocalDateTime.now(),
                kakaKvalitetsvurderingId = UUID.randomUUID(),
                kakaKvalitetsvurderingVersion = 2,
                frist = LocalDate.now().plusWeeks(12),
            )
            assertThat(klagebehandling.getStatus()).isEqualTo(Behandling.Status.FULLFOERT)
        }
    }
}