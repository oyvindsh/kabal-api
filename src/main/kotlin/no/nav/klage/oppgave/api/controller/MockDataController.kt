package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.klage.MottakDokumentType
import no.nav.klage.oppgave.domain.klage.PartIdType
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import no.nav.klage.oppgave.service.MottakService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Profile("dev-gcp")
@RestController
@RequestMapping("mockdata")
class MockDataController(
    private val mottakService: MottakService,
    private val safClient: SafGraphQlClient
) {

    // Alle syntetiske personer under her er registrert under https://dolly.dev.adeo.no/gruppe/2960

    @Unprotected
    @PostMapping("/randomklage")
    fun sendInnRandomKlage() {
        val dollyDoc = listOf(
            SyntheticWithDoc("02446701749", "493357061"),
            SyntheticWithDoc("29437117843", "493357060"),
            SyntheticWithDoc("25438301286", "493357059"),
            SyntheticWithDoc("18496900509", "493357058"),
            SyntheticWithDoc("28416904490", "493357056"),
            SyntheticWithDoc("17457337760", "493357054"),
            SyntheticWithDoc("09418242458", "493357053"),
            SyntheticWithDoc("16498818653", "493357052"),
            SyntheticWithDoc("20467938577", "493357051"),
            SyntheticWithDoc("14437830275", "493357050"),
            SyntheticWithDoc("18418507701", "493357049"),
            SyntheticWithDoc("12518603068", "493357045")
        ).shuffled().first()

        val fnr = dollyDoc.fnr
        val journalpostId = dollyDoc.journalpost
        val journalpost = safClient.getJournalpostAsSystembruker(journalpostId)
        val saknr = journalpost?.sak?.fagsakId

        val dato = LocalDate.of(2020, (1..12).random(), (1..28).random())

        mottakService.createMottakForKlage(
            OversendtKlage(
                tema = listOf(Tema.OMS, Tema.SYK, Tema.FOR, Tema.AAP).shuffled().first(),
                type = Type.KLAGE,
                klager = OversendtKlager(
                    id = OversendtPartId(PartIdType.PERSON, fnr)
                ),
                fagsak = saknr,
                kildeReferanse = "REF_$fnr",
                innsynUrl = "https://nav.no",
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
                tilknyttedeJournalposter = listOf(
                    OversendtDokumentReferanse(
                        randomMottakDokumentType(),
                        journalpostId
                    )
                ),
                mottattFoersteinstans = dato,
                innsendtTilNav = dato.minusDays(3),
                kilde = "OPPGAVE"
            )
        )
    }

    @Unprotected
    @PostMapping("/kode6")
    fun createKode6Person() {
        val fnr = "15436621822" // ÅPENHJERTIG SAKS
        val journalpostId = "493357084"
        val journalpost = safClient.getJournalpost(journalpostId)
        val saknr = journalpost?.sak?.fagsakId
        val dato = LocalDate.of(2020, 1, 13)

        mottakService.createMottakForKlage(
            OversendtKlage(
                tema = Tema.FOR,
                type = Type.KLAGE,
                klager = OversendtKlager(
                    id = OversendtPartId(PartIdType.PERSON, fnr)
                ),
                fagsak = saknr,
                kildeReferanse = "REF_$fnr",
                innsynUrl = "https://nav.no",
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
                tilknyttedeJournalposter = listOf(
                    OversendtDokumentReferanse(
                        randomMottakDokumentType(),
                        journalpostId
                    )
                ),
                mottattFoersteinstans = dato,
                innsendtTilNav = dato.minusDays(3),
                kilde = "OPPGAVE"
            )
        )
    }

    @Unprotected
    @PostMapping("/kode7")
    fun createKode7Person() {
        val fnr = "28107122119" // GOD STAFFELI
        val journalpostId = "493357085"
        val journalpost = safClient.getJournalpost(journalpostId)
        val saknr = journalpost?.sak?.fagsakId
        val dato = LocalDate.of(2020, 1, 13)

        mottakService.createMottakForKlage(
            OversendtKlage(
                tema = Tema.SYK,
                type = Type.KLAGE,
                klager = OversendtKlager(
                    id = OversendtPartId(PartIdType.PERSON, fnr)
                ),
                fagsak = saknr,
                kildeReferanse = "REF_$fnr",
                innsynUrl = "https://nav.no",
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
                tilknyttedeJournalposter = listOf(
                    OversendtDokumentReferanse(
                        randomMottakDokumentType(),
                        journalpostId
                    )
                ),
                mottattFoersteinstans = dato,
                innsendtTilNav = dato.minusDays(3),
                kilde = "OPPGAVE"
            )
        )
    }

    @Unprotected
    @PostMapping("/fullmakt")
    fun createPersonWithFullmakt() {
        val fnr = "17117323862" // SNILL VEPS
        val journalpostId = "493357182"
        val journalpost = safClient.getJournalpost(journalpostId)
        val saknr = journalpost?.sak?.fagsakId
        val dato = LocalDate.of(2020, 1, 13)

        mottakService.createMottakForKlage(
            OversendtKlage(
                tema = Tema.SYK,
                type = Type.KLAGE,
                klager = OversendtKlager(
                    id = OversendtPartId(PartIdType.PERSON, fnr),
                    klagersProsessfullmektig = OversendtProsessfullmektig(
                        id = OversendtPartId(PartIdType.PERSON, "25017820926"),
                        skalKlagerMottaKopi = true
                    )
                ),
                fagsak = saknr,
                kildeReferanse = "REF_$fnr",
                innsynUrl = "https://nav.no",
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
                tilknyttedeJournalposter = listOf(
                    OversendtDokumentReferanse(
                        randomMottakDokumentType(),
                        journalpostId
                    )
                ),
                mottattFoersteinstans = dato,
                innsendtTilNav = dato.minusDays(3),
                kilde = "OPPGAVE"
            )
        )
    }

    private fun randomMottakDokumentType() = listOf(
        MottakDokumentType.OVERSENDELSESBREV,
        MottakDokumentType.ANNET,
        MottakDokumentType.BRUKERS_KLAGE,
        MottakDokumentType.OPPRINNELIG_VEDTAK
    ).shuffled().first()

    data class SyntheticWithDoc(
        val fnr: String,
        val journalpost: String
    )
}
