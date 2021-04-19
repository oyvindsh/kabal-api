package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.domain.klage.MottakDokumentType
import no.nav.klage.oppgave.domain.klage.PartIdType
import no.nav.klage.oppgave.domain.kodeverk.Sakstype
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.service.MottakService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.*
import javax.validation.Valid

@Profile("dev-gcp")
@RestController
@Unprotected
@RequestMapping("testoversendelse")
class UnprotectedTestOversendelseController(
    private val mottakService: MottakService
) {
    @PostMapping("/klage")
    fun sendInnKlage(@Valid @RequestBody oversendtKlage: OversendtKlage) {
        mottakService.createMottakForKlage(oversendtKlage)
    }

    @PostMapping("/randomklage")
    fun sendInnRandomKlage() {
        val fnr = listOf(
            "01498435854",
            "01508420680",
            "03508440684",
            "08528430687",
            "10498400820",
            "13488409241",
            "19508400619",
            "23458435642",
            "23478437471",
            "26488436251",
            "27458422236"
        ).shuffled().first()
        val dato = LocalDate.of(2020, (1..12).random(), (1..28).random())

        mottakService.createMottakForKlage(
            OversendtKlage(
                uuid = UUID.randomUUID(),
                tema = listOf(Tema.SYK).shuffled().first(),
                sakstype = Sakstype.KLAGE,
                klager = OversendtKlager(
                    id = OversendtKlagerPartId(PartIdType.PERSON, fnr)
                ),
                sakReferanse = "10000",
                kildeReferanse = "REF_$fnr",
                innsynUrl = "https://vg.no",
                hjemler = listOf(
                    listOf(
                        HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 4),
                        HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 21),
                        HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 22),
                        HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 35)
                    ).shuffled().first()
                ),
                avsenderSaksbehandlerIdent = "Z994674",
                avsenderEnhet = "0104", //NAV Moss
                oversendtEnhet = "4291",
                tilknyttedeJournalposter = listOf(OversendtDokumentReferanse(MottakDokumentType.ANNET, "123")),
                mottattFoersteinstans = dato,
                innsendtTilNav = dato.minusDays(3),
                frist = dato.plusDays(100),
                kilde = "OPPGAVE"
            )
        )
    }

    @PostMapping("/predefklage")
    fun sendInnKlage() {
        val klager = listOf(
            OversendtKlage(
                uuid = UUID.randomUUID(),
                tema = Tema.SYK,
                sakstype = Sakstype.KLAGE,
                klager = OversendtKlager(
                    id = OversendtKlagerPartId(PartIdType.PERSON, "27458422236")
                ),
                sakReferanse = "10001",
                kildeReferanse = "SYK_27458422236",
                innsynUrl = "https://vg.no",
                hjemler = listOf(
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 4),
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 21)
                ),
                avsenderSaksbehandlerIdent = "Z994674",
                avsenderEnhet = "0104", //NAV Moss
                oversendtEnhet = "4291",
                tilknyttedeJournalposter = listOf(OversendtDokumentReferanse(MottakDokumentType.ANNET, "123")),
                mottattFoersteinstans = LocalDate.of(2021, 2, 2),
                innsendtTilNav = LocalDate.of(2021, 2, 1),
                frist = LocalDate.of(2021, 8, 2),
                kilde = "OPPGAVE"
            ),
            OversendtKlage(
                uuid = UUID.randomUUID(),
                tema = Tema.SYK,
                sakstype = Sakstype.KLAGE,
                klager = OversendtKlager(
                    id = OversendtKlagerPartId(PartIdType.PERSON, "28488425473")
                ),
                sakReferanse = "10002",
                kildeReferanse = "SYK_28488425473",
                innsynUrl = "https://vg.no",
                hjemler = listOf(
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 4),
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 21)
                ),
                avsenderSaksbehandlerIdent = "Z994674",
                avsenderEnhet = "0104", //NAV Moss
                oversendtEnhet = "4291",
                tilknyttedeJournalposter = listOf(OversendtDokumentReferanse(MottakDokumentType.ANNET, "123")),
                mottattFoersteinstans = LocalDate.of(2021, 2, 3),
                innsendtTilNav = LocalDate.of(2021, 2, 2),
                frist = LocalDate.of(2021, 8, 3),
                kilde = "OPPGAVE"
            ),
            OversendtKlage(
                uuid = UUID.randomUUID(),
                tema = Tema.SYK,
                sakstype = Sakstype.KLAGE,
                klager = OversendtKlager(
                    id = OversendtKlagerPartId(PartIdType.PERSON, "02518418680")
                ),
                sakReferanse = "10003",
                kildeReferanse = "SYK_02518418680",
                innsynUrl = "https://vg.no",
                hjemler = listOf(
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 4),
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 21)
                ),
                avsenderSaksbehandlerIdent = "Z994674",
                avsenderEnhet = "0104", //NAV Moss
                oversendtEnhet = "4291",
                tilknyttedeJournalposter = listOf(OversendtDokumentReferanse(MottakDokumentType.ANNET, "123")),
                mottattFoersteinstans = LocalDate.of(2021, 2, 4),
                innsendtTilNav = LocalDate.of(2021, 2, 3),
                frist = LocalDate.of(2021, 8, 4),
                kilde = "OPPGAVE"
            ),
            OversendtKlage(
                uuid = UUID.randomUUID(),
                tema = Tema.SYK,
                sakstype = Sakstype.KLAGE,
                klager = OversendtKlager(
                    id = OversendtKlagerPartId(PartIdType.PERSON, "02508425425")
                ),
                sakReferanse = "10004",
                kildeReferanse = "SYK_02508425425",
                innsynUrl = "https://vg.no",
                hjemler = listOf(
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 4),
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 21)
                ),
                avsenderSaksbehandlerIdent = "Z994674",
                avsenderEnhet = "0104", //NAV Moss
                oversendtEnhet = "4291",
                tilknyttedeJournalposter = listOf(OversendtDokumentReferanse(MottakDokumentType.ANNET, "123")),
                mottattFoersteinstans = LocalDate.of(2021, 2, 5),
                innsendtTilNav = LocalDate.of(2021, 2, 4),
                frist = LocalDate.of(2021, 8, 5),
                kilde = "OPPGAVE"
            ),
            OversendtKlage(
                uuid = UUID.randomUUID(),
                tema = Tema.SYK,
                sakstype = Sakstype.KLAGE,
                klager = OversendtKlager(
                    id = OversendtKlagerPartId(PartIdType.PERSON, "23528406688"),
                    klagersProsessfullmektig = OversendtPart(
                        id = OversendtKlagerPartId(PartIdType.PERSON, "8301839832"),
                        skalKlagerMottaKopi = true
                    )
                ),
                sakReferanse = "10005",
                kildeReferanse = "SYK_23528406688",
                innsynUrl = "https://vg.no",
                hjemler = listOf(
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 4),
                    HjemmelFraFoersteInstans(Lov.FOLKETRYGDLOVEN, 8, 21)
                ),
                avsenderSaksbehandlerIdent = "Z994674",
                avsenderEnhet = "0104", //NAV Moss
                oversendtEnhet = "4291",
                tilknyttedeJournalposter = listOf(OversendtDokumentReferanse(MottakDokumentType.ANNET, "123")),
                mottattFoersteinstans = LocalDate.of(2021, 2, 6),
                innsendtTilNav = LocalDate.of(2021, 2, 5),
                frist = LocalDate.of(2021, 8, 6),
                kilde = "OPPGAVE"
            )
        )
        klager.forEach {
            mottakService.createMottakForKlage(it)
        }
    }
}
