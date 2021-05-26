package no.nav.klage.oppgave.service.distribusjon

import com.ninjasquad.springmockk.MockkBean
import no.nav.klage.oppgave.clients.dokdistfordeling.DokDistFordelingClient
import no.nav.klage.oppgave.db.TestPostgresqlContainer
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.*
import no.nav.klage.oppgave.repositories.KafkaVedtakEventRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.service.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionCallbackWithoutResult
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


@ActiveProfiles("local")
@Import(KlagebehandlingDistribusjonServiceTest.MyTestConfiguration::class)
@SpringBootTest(classes = [KlagebehandlingService::class, VedtakDistribusjonService::class, KlagebehandlingDistribusjonService::class])
@EnableJpaRepositories(basePackages = ["no.nav.klage.oppgave.repositories"])
@EntityScan("no.nav.klage.oppgave.domain")
@AutoConfigureDataJpa
@Testcontainers
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@AutoConfigureTestEntityManager
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class KlagebehandlingDistribusjonServiceTest {

    companion object {
        @Container
        @JvmField
        val postgreSQLContainer: TestPostgresqlContainer = TestPostgresqlContainer.instance
    }

    @Configuration
    internal class MyTestConfiguration {

        @MockkBean(relaxed = true)
        lateinit var tilgangService: TilgangService

        @MockkBean(relaxed = true)
        lateinit var applicationEventPublisher: ApplicationEventPublisher

        @MockkBean(relaxed = true)
        lateinit var dokumentService: DokumentService

        @MockkBean(relaxed = true)
        lateinit var tokenService: TokenService

        @MockkBean(relaxed = true)
        lateinit var kafkaVedtakEventRepository: KafkaVedtakEventRepository

        @MockkBean(relaxed = true)
        lateinit var vedtakKafkaProducer: VedtakKafkaProducer

        @MockkBean(relaxed = true)
        lateinit var dokDistFordelingClient: DokDistFordelingClient

        @MockkBean(relaxed = true)
        lateinit var klagebehandlingAvslutningService: KlagebehandlingAvslutningService
    }

    //@Autowired
    //lateinit var entityManager: EntityManager
    //lateinit var entityManager: TestEntityManager

    @Autowired
    lateinit var klagebehandlingRepository: KlagebehandlingRepository

    @Autowired
    lateinit var mottakRepository: MottakRepository

    @Autowired
    lateinit var klagebehandlingDistribusjonService: KlagebehandlingDistribusjonService

    @Autowired
    lateinit var txTemplate: TransactionTemplate

    private val klagebehandlingId = UUID.randomUUID()

    @Test
    @Order(1)
    @Rollback(false)
    fun `save klagebehandling`() {
        txTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(status: TransactionStatus?) {
                val mottak = Mottak(
                    tema = Tema.OMS,
                    type = Type.KLAGE,
                    kildesystem = Fagsystem.FS39,
                    kildeReferanse = "1234234",
                    klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
                    oversendtKaDato = LocalDateTime.now()
                )

                mottakRepository.save(mottak)

                val klage = Klagebehandling(
                    id = klagebehandlingId,
                    klager = Klager(partId = PartId(type = PartIdType.PERSON, value = "23452354")),
                    sakenGjelder = SakenGjelder(
                        partId = PartId(type = PartIdType.PERSON, value = "23452354"),
                        skalMottaKopi = false
                    ),
                    tema = Tema.OMS,
                    type = Type.KLAGE,
                    frist = LocalDate.now(),
                    hjemler = mutableSetOf(
                        Hjemmel.FTL_8_7
                    ),
                    created = LocalDateTime.now(),
                    modified = LocalDateTime.now(),
                    mottattKlageinstans = LocalDateTime.now(),
                    kildesystem = Fagsystem.FS39,
                    mottakId = mottak.id,
                    vedtak = mutableSetOf(Vedtak(journalpostId = "1"))
                )

                klagebehandlingRepository.save(klage)
            }
        })

        txTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(status: TransactionStatus?) {
                klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandlingId)
            }
        })

        txTemplate.execute(object : TransactionCallbackWithoutResult() {
            override fun doInTransactionWithoutResult(status: TransactionStatus?) {
                val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
                println(klagebehandling.versjon)
            }
        })

    }

    @Test
    @Order(2)
    @Rollback(false)
    fun `distribuer klagebehandling`() {
        //klagebehandlingDistribusjonService.distribuerKlagebehandling(klagebehandlingId)
    }

    @Test
    @Order(3)
    @Rollback(false)
    fun `sjekk klagebehandling`() {
        //val klagebehandling = klagebehandlingRepository.getOne(klagebehandlingId)
        //println(klagebehandling.versjon)
    }

}