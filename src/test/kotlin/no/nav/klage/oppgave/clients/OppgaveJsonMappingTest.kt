package no.nav.klage.oppgave.clients

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.klage.oppgave.clients.gosys.OppgaveResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.json.JsonTest


@JsonTest
class OppgaveJsonMappingTest(val objectMapper: ObjectMapper) {

    @Test
    fun `skal klare å mappe json fra oppgave inkl ident`() {

        val response: OppgaveResponse = objectMapper.readValue(jsonResponse)
        assertThat(response.oppgaver.first().identer).isNotNull
    }

    val jsonResponse: String =
        """
    {
        "antallTreffTotalt": 10290,
        "oppgaver": [
        {
            "id": 104487969,
            "tildeltEnhetsnr": "2821",
            "endretAvEnhetsnr": "0316",
            "opprettetAvEnhetsnr": "0300",
            "aktoerId": "1000074827905",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "behandlingstema": "ab0326",
            "oppgavetype": "ORI_SAK_UTB",
            "versjon": 2,
            "opprettetAv": "C128375",
            "endretAv": "W113306",
            "prioritet": "NORM",
            "status": "OPPRETTET",
            "metadata": {},
            "fristFerdigstillelse": "2010-03-15",
            "aktivDato": "2010-03-15",
            "opprettetTidspunkt": "2010-03-15T08:07:38.463+01:00",
            "endretTidspunkt": "2010-03-19T17:19:29.276+01:00"
        },
        {
            "id": 101573355,
            "tildeltEnhetsnr": "1556",
            "endretAvEnhetsnr": "1589",
            "opprettetAvEnhetsnr": "1535",
            "aktoerId": "1000009222161",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "oppgavetype": "GI_VEI_INFO",
            "versjon": 6,
            "opprettetAv": "J115682",
            "endretAv": "S119261",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {},
            "fristFerdigstillelse": "2009-05-18",
            "aktivDato": "2009-05-11",
            "opprettetTidspunkt": "2009-05-11T13:36:04.356+02:00",
            "endretTidspunkt": "2010-09-29T09:08:36.917+02:00"
        },
        {
            "id": 114757674,
            "tildeltEnhetsnr": "2850",
            "endretAvEnhetsnr": "4101",
            "opprettetAvEnhetsnr": "4101",
            "aktoerId": "1000039592215",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "behandlingstema": "ab0326",
            "oppgavetype": "KONT_BRUK",
            "versjon": 1,
            "opprettetAv": "L123318",
            "endretAv": "L123318",
            "prioritet": "NORM",
            "status": "OPPRETTET",
            "metadata": {},
            "fristFerdigstillelse": "2011-09-15",
            "aktivDato": "2011-09-13",
            "opprettetTidspunkt": "2011-09-13T08:58:55.853+02:00",
            "endretTidspunkt": "2011-09-13T08:58:55.853+02:00"
        },
        {
            "id": 124346776,
            "tildeltEnhetsnr": "2821",
            "endretAvEnhetsnr": "0889",
            "opprettetAvEnhetsnr": "0828",
            "aktoerId": "1000023581733",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "behandlingstema": "ab0342",
            "oppgavetype": "VUR",
            "versjon": 2,
            "opprettetAv": "H119406",
            "endretAv": "A136200",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {},
            "fristFerdigstillelse": "2012-06-28",
            "aktivDato": "2012-06-27",
            "opprettetTidspunkt": "2012-06-27T11:12:39.835+02:00",
            "endretTidspunkt": "2012-06-28T09:53:19.303+02:00"
        },
        {
            "id": 207671738,
            "tildeltEnhetsnr": "2910",
            "endretAvEnhetsnr": "2910",
            "opprettetAvEnhetsnr": "2910",
            "aktoerId": "1000055434314",
            "tilordnetRessurs": "S108208",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "oppgavetype": "SVAR_IK_MOT",
            "versjon": 1,
            "opprettetAv": "srvdialogstyring",
            "endretAv": "srvdialogstyring",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {
            "EKSTERN_HENVENDELSE_ID": "1000FX6AV"
        },
            "fristFerdigstillelse": "2018-03-07",
            "aktivDato": "2018-03-05",
            "opprettetTidspunkt": "2018-03-05T04:01:58.026+01:00",
            "endretTidspunkt": "2018-03-05T04:01:58.026+01:00"
        },
        {
            "id": 208645712,
            "tildeltEnhetsnr": "2910",
            "endretAvEnhetsnr": "2910",
            "opprettetAvEnhetsnr": "2910",
            "aktoerId": "1000055434314",
            "tilordnetRessurs": "S108208",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "oppgavetype": "SVAR_IK_MOT",
            "versjon": 1,
            "opprettetAv": "srvdialogstyring",
            "endretAv": "srvdialogstyring",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {
            "EKSTERN_HENVENDELSE_ID": "1000G9UKE"
        },
            "fristFerdigstillelse": "2018-03-27",
            "aktivDato": "2018-03-24",
            "opprettetTidspunkt": "2018-03-24T04:02:17.499+01:00",
            "endretTidspunkt": "2018-03-24T04:02:17.499+01:00"
        },
        {
            "id": 208812030,
            "tildeltEnhetsnr": "4847",
            "endretAvEnhetsnr": "4847",
            "opprettetAvEnhetsnr": "4847",
            "aktoerId": "1000075194739",
            "tilordnetRessurs": "S118949",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "oppgavetype": "SVAR_IK_MOT",
            "versjon": 1,
            "opprettetAv": "srvdialogstyring",
            "endretAv": "srvdialogstyring",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {
            "EKSTERN_HENVENDELSE_ID": "1000GE8X0"
        },
            "fristFerdigstillelse": "2018-04-04",
            "aktivDato": "2018-03-31",
            "opprettetTidspunkt": "2018-03-31T04:01:47.7+02:00",
            "endretTidspunkt": "2018-03-31T04:01:47.7+02:00"
        },
        {
            "id": 209647081,
            "tildeltEnhetsnr": "4806",
            "endretAvEnhetsnr": "4806",
            "opprettetAvEnhetsnr": "4806",
            "aktoerId": "1000066514087",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "behandlingstema": "ab0342",
            "oppgavetype": "MAN_LOP_UTB",
            "versjon": 1,
            "opprettetAv": "T117123",
            "endretAv": "T117123",
            "prioritet": "NORM",
            "status": "OPPRETTET",
            "metadata": {},
            "fristFerdigstillelse": "2020-04-02",
            "aktivDato": "2018-04-19",
            "opprettetTidspunkt": "2018-04-19T09:26:21.693+02:00",
            "endretTidspunkt": "2018-04-19T09:26:21.693+02:00"
        },
        {
            "id": 213351025,
            "tildeltEnhetsnr": "4847",
            "endretAvEnhetsnr": "4847",
            "opprettetAvEnhetsnr": "4847",
            "aktoerId": "1000005157837",
            "tilordnetRessurs": "S121070",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "oppgavetype": "SVAR_IK_MOT",
            "versjon": 1,
            "opprettetAv": "srvdialogstyring",
            "endretAv": "srvdialogstyring",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {
            "EKSTERN_HENVENDELSE_ID": "1000i5BJM"
        },
            "fristFerdigstillelse": "2018-07-18",
            "aktivDato": "2018-07-16",
            "opprettetTidspunkt": "2018-07-16T04:02:01.507+02:00",
            "endretTidspunkt": "2018-07-16T04:02:01.507+02:00"
        },
        {
            "id": 215140410,
            "tildeltEnhetsnr": "4847",
            "endretAvEnhetsnr": "4847",
            "opprettetAvEnhetsnr": "4847",
            "aktoerId": "1000010726309",
            "tilordnetRessurs": "H103158",
            "beskrivelse": "MASKERT",
            "tema": "FOR",
            "oppgavetype": "VUR_SVAR",
            "versjon": 1,
            "opprettetAv": "srvdialogstyring",
            "endretAv": "srvdialogstyring",
            "prioritet": "HOY",
            "status": "OPPRETTET",
            "metadata": {
            "EKSTERN_HENVENDELSE_ID": "1000GHC5K"
        },
            "fristFerdigstillelse": "2018-09-06",
            "aktivDato": "2018-09-04",
            "opprettetTidspunkt": "2018-09-04T12:37:48.814+02:00",
            "endretTidspunkt": "2018-09-04T12:37:48.814+02:00"
        }
        ]
    }
    """.trimIndent()

}