package no.nav.klage.oppgave.api.controller

import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseTilHjemler
import no.nav.klage.kodeverk.hjemmel.ytelseTilRegistreringshjemlerV1
import no.nav.klage.kodeverk.hjemmel.ytelseTilRegistreringshjemlerV2
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.kafka.ExternalUtfall
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandlingInput
import no.nav.klage.oppgave.domain.klage.MottakDokumentType
import no.nav.klage.oppgave.domain.klage.utfallToTrygderetten
import no.nav.klage.oppgave.service.AnkeITrygderettenbehandlingService
import no.nav.klage.oppgave.service.MottakService
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

@Profile("dev-gcp")
@RestController
@RequestMapping("mockdata")
class MockDataController(
    private val mottakService: MottakService,
    private val safClient: SafGraphQlClient,
    private val ankeITrygderettenbehandlingService: AnkeITrygderettenbehandlingService,
    @Value("#{T(java.time.LocalDate).parse('\${KAKA_VERSION_2_DATE}')}")
    private val kakaVersion2Date: LocalDate,
) {

    //https://dolly.ekstern.dev.nav.no/gruppe/6336
    @Unprotected
    @PostMapping("/kode6")
    fun createKode6Person() {
        createKlagebehandlingForASpecificPerson("26876597755")
    }

    //https://dolly.ekstern.dev.nav.no/gruppe/6335
    @Unprotected
    @PostMapping("/kode7")
    fun createKode7Person() {
        createKlagebehandlingForASpecificPerson("17855999285")
    }

    //https://dolly.ekstern.dev.nav.no/gruppe/6334
    @Unprotected
    @PostMapping("/egenansatt")
    fun createEgenAnsattBehandling() {
        createKlagebehandlingForASpecificPerson("12518812945")
    }

    fun createKlagebehandlingForASpecificPerson(fnr: String) {
        val journalpostId = "510534809"
        val journalpost = safClient.getJournalpostAsSystembruker(journalpostId)
        val dato = LocalDate.of(2022, 1, 13)

        mottakService.createMottakForKlageAnkeV3(
            OversendtKlageAnkeV3(
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                klager = OversendtKlager(
                    id = OversendtPartId(OversendtPartIdType.PERSON, fnr)
                ),
                fagsak = OversendtSak(
                    fagsakId = journalpost.sak?.fagsakId ?: "UKJENT",
                    fagsystem = journalpost.sak?.fagsaksystem?.let {
                        try {
                            KildeFagsystem.valueOf(it)
                        } catch (e: Exception) {
                            KildeFagsystem.AO01
                        }
                    }
                        ?: KildeFagsystem.AO01
                ),
                kildeReferanse = UUID.randomUUID().toString(),
                innsynUrl = "https://nav.no",
                hjemler = listOf(
                    listOf(
                        Hjemmel.FTRL_9_10,
                    ).shuffled().first()
                ),
                forrigeBehandlendeEnhet = "0104", //NAV Moss
                tilknyttedeJournalposter = listOf(
                    OversendtDokumentReferanse(
                        randomMottakDokumentType(),
                        journalpostId
                    )
                ),
                brukersHenvendelseMottattNavDato = dato,
                innsendtTilNav = dato.minusDays(3),
                kilde = KildeFagsystem.AO01
            )
        )
    }

    @Unprotected
    @PostMapping("/fullmakt")
    fun createPersonWithFullmakt() {
        val fnr = "28497037273"
        val journalpostId = "510534808"
        val journalpost = safClient.getJournalpostAsSystembruker(journalpostId)
        val dato = LocalDate.of(2020, 1, 13)

        mottakService.createMottakForKlageAnkeV3(
            OversendtKlageAnkeV3(
                ytelse = Ytelse.OMS_OMP,
                type = Type.KLAGE,
                klager = OversendtKlager(
                    id = OversendtPartId(OversendtPartIdType.PERSON, fnr),
                    klagersProsessfullmektig = OversendtProsessfullmektig(
                        id = OversendtPartId(OversendtPartIdType.PERSON, "07467517958"),
                        skalKlagerMottaKopi = true
                    )
                ),
                fagsak = OversendtSak(
                    fagsakId = journalpost.sak?.fagsakId ?: "UKJENT",
                    fagsystem = journalpost.sak?.fagsaksystem?.let {
                        try {
                            KildeFagsystem.valueOf(it)
                        } catch (e: Exception) {
                            KildeFagsystem.AO01
                        }
                    }
                        ?: KildeFagsystem.AO01
                ),
                kildeReferanse = UUID.randomUUID().toString(),
                innsynUrl = "https://nav.no",
                hjemler = listOf(
                    listOf(
                        Hjemmel.FTRL_9_10,
                    ).shuffled().first()
                ),
                forrigeBehandlendeEnhet = "0104", //NAV Moss
                tilknyttedeJournalposter = listOf(
                    OversendtDokumentReferanse(
                        randomMottakDokumentType(),
                        journalpostId
                    )
                ),
                brukersHenvendelseMottattNavDato = dato,
                innsendtTilNav = dato.minusDays(3),
                kilde = KildeFagsystem.AO01
            )
        )
    }

    @Unprotected
    @PostMapping("/randomklage")
    fun sendInnRandomKlage(
        @RequestBody(required = false) input: MockInput? = null
    ): MockDataResponse {
        return createKlanke(Type.KLAGE, input)
    }

    @Unprotected
    @PostMapping("/randomanke")
    fun sendInnRandomAnke(
        @RequestBody(required = false) input: MockInput? = null
    ): MockDataResponse {
        return createKlanke(Type.ANKE, input)
    }

    @Unprotected
    @PostMapping("/randomankeitrygderetten")
    fun sendInnRandomAnkeITrygderetten(
        @RequestBody(required = false) input: MockInput? = null
    ): MockDataResponse {
        return createKlanke(Type.ANKE_I_TRYGDERETTEN, input)
    }

    data class MockDataResponse(
        val id: UUID,
        val typeId: String,
        val ytelseId: String,
        val hjemmelId: String,
    )

    //https://dolly.ekstern.dev.nav.no/gruppe/6332
    private fun getFnrAndJournalpostId(ytelse: Ytelse): FnrAndJournalpostId {
        return when (ytelse) {
            Ytelse.ENF_ENF -> FnrAndJournalpostId(
                fnr = "17887799784", journalpostId = "598126218"
            )

            Ytelse.BAR_BAR -> FnrAndJournalpostId(
                fnr = "50884800363", journalpostId = "598126221"
            )

            Ytelse.KON_KON -> FnrAndJournalpostId(
                fnr = "09868799487", journalpostId = "598126222"
            )

            Ytelse.OMS_OLP, Ytelse.OMS_OMP, Ytelse.OMS_PLS, Ytelse.OMS_PSB -> FnrAndJournalpostId(
                fnr = "22816897630", journalpostId = "598126223"
            )

            Ytelse.SYK_SYK -> FnrAndJournalpostId(
                fnr = "15896899446", journalpostId = "598126077"
            )

            Ytelse.SUP_UFF -> FnrAndJournalpostId(
                fnr = "24898299771", journalpostId = "598126224"
            )

            Ytelse.FOR_ENG, Ytelse.FOR_FOR, Ytelse.FOR_SVA -> FnrAndJournalpostId(
                fnr = "14828897927", journalpostId = "598126225"
            )

            else -> FnrAndJournalpostId(
                fnr = "17887799784", journalpostId = "598126218"
            )
        }
    }

    data class FnrAndJournalpostId(
        val fnr: String,
        val journalpostId: String
    )

    private fun createKlanke(type: Type, mockInput: MockInput?): MockDataResponse {
        val ytelse = if (mockInput == null) Ytelse.SYK_SYK else mockInput.ytelse ?: ytelseTilHjemler.keys.random()

        val fnrAndJournalpostId = getFnrAndJournalpostId(ytelse)

        val fnr = fnrAndJournalpostId.fnr
        val journalpostId = fnrAndJournalpostId.journalpostId
        val lastMonth = LocalDate.now().minusMonths(1).toEpochDay()
        val now = LocalDate.now().toEpochDay()
        val dato = LocalDate.ofEpochDay(ThreadLocalRandom.current().nextLong(lastMonth, now))

        val klager = mockInput?.klager ?: OversendtKlager(
            id = OversendtPartId(OversendtPartIdType.PERSON, fnr)
        )

        val sakenGjelder = mockInput?.sakenGjelder

        val oversendtSak = OversendtSak(
            fagsakId = Random.nextInt(from = 1, until = 9999).toString(),
            fagsystem = KildeFagsystem.AO01

        )

        val behandling = when (type) {
            Type.KLAGE, Type.ANKE -> {
                mottakService.createMottakForKlageAnkeV3ForE2ETests(
                    OversendtKlageAnkeV3(
                        ytelse = ytelse,
                        type = type,
                        klager = klager,
                        fagsak = oversendtSak,
                        sakenGjelder = sakenGjelder,
                        kildeReferanse = mockInput?.kildeReferanse ?: UUID.randomUUID().toString(),
                        dvhReferanse = mockInput?.dvhReferanse,
                        innsynUrl = "https://nav.no",
                        hjemler = listOf(ytelseTilHjemler[ytelse]!!.random()),
                        forrigeBehandlendeEnhet = mockInput?.forrigeBehandlendeEnhet ?: "4295", //NAV Klageinstans nord
                        tilknyttedeJournalposter = listOf(
                            OversendtDokumentReferanse(
                                MottakDokumentType.BRUKERS_KLAGE,
                                journalpostId
                            )
                        ),
                        brukersHenvendelseMottattNavDato = dato,
                        sakMottattKaDato = dato,
                        innsendtTilNav = dato.minusDays(3),
                        kilde = KildeFagsystem.AO01,
                    )
                )
            }

            Type.ANKE_I_TRYGDERETTEN -> {
                val registreringsHjemmelSet = when (getKakaVersion()) {
                    1 -> {
                        mutableSetOf(ytelseTilRegistreringshjemlerV1[ytelse]!!.random())
                    }

                    2 -> {
                        mutableSetOf(ytelseTilRegistreringshjemlerV2[ytelse]!!.random())
                    }

                    else ->
                        throw error("wrong version")
                }

                val input = AnkeITrygderettenbehandlingInput(
                    klager = klager.toKlagepart(),
                    sakenGjelder = sakenGjelder?.toSakenGjelder(),
                    ytelse = ytelse,
                    type = type,
                    kildeReferanse = mockInput?.kildeReferanse ?: UUID.randomUUID().toString(),
                    dvhReferanse = mockInput?.dvhReferanse ?: UUID.randomUUID().toString(),
                    fagsystem = Fagsystem.fromNavn(oversendtSak.fagsystem.name),
                    fagsakId = oversendtSak.fagsakId,
                    sakMottattKlageinstans = dato.atStartOfDay(),
                    saksdokumenter = mutableSetOf(),
                    innsendingsHjemler = mutableSetOf(ytelseTilHjemler[ytelse]!!.random()),
                    sendtTilTrygderetten = LocalDateTime.now(),
                    registreringsHjemmelSet = registreringsHjemmelSet,
                    ankebehandlingUtfall = ExternalUtfall.valueOf(utfallToTrygderetten.random().name),
                )

                ankeITrygderettenbehandlingService.createAnkeITrygderettenbehandling(
                    input = input
                )
            }
        }

        return MockDataResponse(
            id = behandling.id,
            typeId = behandling.type.id,
            ytelseId = behandling.ytelse.id,
            hjemmelId = behandling.hjemler.first().id
        )
    }

    private fun getKakaVersion(): Int {
        val kvalitetsvurderingVersion = if (LocalDate.now() >= kakaVersion2Date) {
            2
        } else {
            1
        }
        return kvalitetsvurderingVersion
    }

    private fun randomMottakDokumentType() = listOf(
        MottakDokumentType.OVERSENDELSESBREV,
        MottakDokumentType.ANNET,
        MottakDokumentType.BRUKERS_KLAGE,
        MottakDokumentType.OPPRINNELIG_VEDTAK
    ).shuffled().first()

    data class MockInput(
        val ytelse: Ytelse?,
        val klager: OversendtKlager?,
        val sakenGjelder: OversendtSakenGjelder?,
        val kildeReferanse: String?,
        val dvhReferanse: String?,
        val forrigeBehandlendeEnhet: String?,
    )
}
