package no.nav.klage.oppgave.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import no.nav.klage.oppgave.config.ElasticsearchServiceConfiguration
import no.nav.klage.oppgave.domain.KlagebehandlingerSearchCriteria
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling
import no.nav.klage.oppgave.domain.elasticsearch.EsKlagebehandling.Status.IKKE_TILDELT
import no.nav.klage.oppgave.domain.kodeverk.MedunderskriverFlyt
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import no.nav.klage.oppgave.repositories.EsKlagebehandlingRepository
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.assertj.core.api.Assertions.assertThat
import org.elasticsearch.index.query.QueryBuilders
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.SearchHits
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder
import org.springframework.data.elasticsearch.core.query.Query
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime


@ActiveProfiles("local")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@Testcontainers
@SpringBootTest(classes = [ElasticsearchServiceConfiguration::class])
@ImportAutoConfiguration(
    ElasticsearchRestClientAutoConfiguration::class,
    ElasticsearchDataAutoConfiguration::class
)
class FortroligElasticsearchServiceTest {

    companion object {
        @Container
        @JvmField
        val esContainer: TestElasticsearchContainer = TestElasticsearchContainer.instance
    }

    @MockkBean(relaxed = true)
    lateinit var innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository

    @Autowired
    lateinit var service: ElasticsearchService

    @Autowired
    lateinit var esTemplate: ElasticsearchRestTemplate

    @Autowired
    lateinit var repo: EsKlagebehandlingRepository

    @Test
    @Order(1)
    fun `es is running`() {
        assertThat(esContainer.isRunning).isTrue
        service.recreateIndex()
    }

    @Test
    @Order(2)
    fun `index has been created by service`() {

        val indexOps = esTemplate.indexOps(IndexCoordinates.of("klagebehandling"))
        assertThat(indexOps.exists()).isTrue()
    }

    private val idNormal = "1001L"

    private val idFortrolig = "1002L"

    private val idStrengtFortrolig = "1003L"

    private val idEgenAnsatt = "1004L"

    private val idEgenAnsattOgFortrolig = "1005L"

    private val idEgenAnsattOgStrengtFortrolig = "1006L"

    @Test
    @Order(3)
    fun `lagrer oppgaver for senere tester`() {

        val normalPerson = EsKlagebehandling(
            id = idNormal,
            tildeltEnhet = "4219",
            tema = Tema.OMS.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
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
            status = IKKE_TILDELT,
            sakenGjelderFnr = "123",
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.navn
        )
        val fortroligPerson =
            EsKlagebehandling(
                id = idFortrolig,
                tildeltEnhet = "4219",
                tema = Tema.OMS.id,
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
                temaNavn = Tema.OMS.name,
                typeNavn = Type.KLAGE.name,
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                fortrolig = true,
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.navn
            )
        val strengtFortroligPerson = EsKlagebehandling(
            id = idStrengtFortrolig,
            tildeltEnhet = "4219",
            tema = Tema.OMS.id,
            type = Type.KLAGE.id,
            tildeltSaksbehandlerident = null,
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
            status = IKKE_TILDELT,
            sakenGjelderFnr = "123",
            strengtFortrolig = true,
            medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.navn
        )
        val egenAnsattPerson =
            EsKlagebehandling(
                id = idEgenAnsatt,
                tildeltEnhet = "4219",
                tema = Tema.OMS.id,
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
                temaNavn = Tema.OMS.name,
                typeNavn = Type.KLAGE.name,
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                egenAnsatt = true,
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.navn
            )
        val egenAnsattOgFortroligPerson =
            EsKlagebehandling(
                id = idEgenAnsattOgFortrolig,
                tildeltEnhet = "4219",
                tema = Tema.OMS.id,
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
                temaNavn = Tema.OMS.name,
                typeNavn = Type.KLAGE.name,
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                egenAnsatt = true,
                fortrolig = true,
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.navn
            )
        val egenAnsattOgStrengtFortroligPerson =
            EsKlagebehandling(
                id = idEgenAnsattOgStrengtFortrolig,
                tildeltEnhet = "4219",
                tema = Tema.OMS.id,
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
                temaNavn = Tema.OMS.name,
                typeNavn = Type.KLAGE.name,
                status = IKKE_TILDELT,
                sakenGjelderFnr = "123",
                egenAnsatt = true,
                strengtFortrolig = true,
                medunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT.navn
            )
        repo.save(normalPerson)
        repo.save(fortroligPerson)
        repo.save(strengtFortroligPerson)
        repo.save(egenAnsattPerson)
        repo.save(egenAnsattOgFortroligPerson)
        repo.save(egenAnsattOgStrengtFortroligPerson)

        val query: Query = NativeSearchQueryBuilder()
            .withQuery(QueryBuilders.matchAllQuery())
            .build()
        val searchHits: SearchHits<EsKlagebehandling> = esTemplate.search(query, EsKlagebehandling::class.java)
        assertThat(searchHits.totalHits).isEqualTo(6L)
    }

    @Test
    @Order(4)
    fun `Saksbehandler with no special rights will only see normal klagebehandlinger`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns false
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(idNormal)
    }

    @Test
    @Order(5)
    fun `Saksbehandler with egen ansatt rights will only see normal klagebehandlinger and those for egen ansatte, but not egen ansatte that are fortrolig or strengt fortrolig`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns false
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(idNormal, idEgenAnsatt)
    }

    @Test
    @Order(6)
    fun `Saksbehandler with fortrolig rights will see normale klagebehandlinger and fortrolige klagebehandlinger, including the combo fortrolig and egen ansatt`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns false
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(
            idNormal, idFortrolig,
            idEgenAnsattOgFortrolig
        )
    }

    @Test
    @Order(7)
    fun `Saksbehandler with fortrolig rights and egen ansatt rights will see normale klagebehandling, fortrolige klagebehandlinger and egen ansatt klagebehandlinger`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns false
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(
            idNormal, idFortrolig, idEgenAnsatt,
            idEgenAnsattOgFortrolig
        )
    }

    @Test
    @Order(8)
    fun `Saksbehandler with strengt fortrolig rights and egen ansatt rights will see strengt fortrolige klagebehandlinger, including the combo strengt fortrolig and egen ansatt`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns true
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(
            idStrengtFortrolig, idEgenAnsattOgStrengtFortrolig
        )
    }

    @Test
    @Order(9)
    fun `Saksbehandler with strengt fortrolig rights without egen ansatt rights will only see strengt fortrolige klagebehandlinger, including the combo strengt fortrolig and egen ansatt`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns true
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(
            idStrengtFortrolig,
            idEgenAnsattOgStrengtFortrolig
        )
    }

    @Test
    @Order(10)
    fun `Saksbehandler with fortrolig and strengt fortrolig rights will only see strengt fortrolige and fortrolige klagebehandlinger, including those that also are egen ansatte`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns false
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns true
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(
            idStrengtFortrolig,
            idEgenAnsattOgStrengtFortrolig,
            idFortrolig,
            idEgenAnsattOgFortrolig
        )
    }

    @Test
    @Order(11)
    fun `Saksbehandler with fortrolig and strengt fortrolig and egen ansatt rights will only see strengt fortrolige and fortrolige klagebehandlinger, including those that also are egen ansatte`() {
        every { innloggetSaksbehandlerRepository.kanBehandleEgenAnsatt() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleFortrolig() } returns true
        every { innloggetSaksbehandlerRepository.kanBehandleStrengtFortrolig() } returns true
        val klagebehandlinger: List<EsKlagebehandling> =
            service.findByCriteria(
                KlagebehandlingerSearchCriteria(
                    temaer = listOf(Tema.OMS),
                    offset = 0,
                    limit = 10
                )
            ).searchHits.map { it.content }
        assertThat(klagebehandlinger.map { it.id }).containsExactlyInAnyOrder(
            idStrengtFortrolig,
            idEgenAnsattOgStrengtFortrolig,
            idFortrolig,
            idEgenAnsattOgFortrolig
        )
    }
}