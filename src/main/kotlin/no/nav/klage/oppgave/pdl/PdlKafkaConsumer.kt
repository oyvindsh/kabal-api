package no.nav.klage.oppgave.pdl

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.klage.oppgave.config.PartitionFinder
import no.nav.klage.oppgave.pdl.dtos.GraderingDto
import no.nav.klage.oppgave.pdl.dtos.IdentGruppeDto
import no.nav.klage.oppgave.pdl.dtos.PdlDokument
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.stereotype.Component


@Component
class PdlKafkaConsumer(
    private val pdlPersonService: PdlPersonService,
    @Qualifier("pdlPersonFinder") private val pdlPersonFinder: PartitionFinder<Any, Any>
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val mapper = ObjectMapper().registerModule(KotlinModule()).registerModule(JavaTimeModule())
    }

    @KafkaListener(
        id = "klagePdlPersonListener",
        idIsGroup = false,
        containerFactory = "pdlPersonKafkaListenerContainerFactory",
        topicPartitions = [TopicPartition(
            topic = "\${PDL_PERSON_KAFKA_TOPIC}",
            partitions = ["#{@pdlPersonFinder.partitions('\${PDL_PERSON_KAFKA_TOPIC}')}"],
            partitionOffsets = [PartitionOffset(partition = "*", initialOffset = "0")]
        )]
    )
    fun listen(pdlDocumentRecord: ConsumerRecord<String, String>) {
        runCatching {
            logger.debug("Reading offset ${pdlDocumentRecord.offset()} from partition ${pdlDocumentRecord.partition()} on kafka topic ${pdlDocumentRecord.topic()}")
            val aktoerId = pdlDocumentRecord.key()
            if (pdlDocumentRecord.value() == null) {
                logger.debug("Received tombstone for aktoerId $aktoerId")
            } else {
                val pdlDokument: PdlDokument = pdlDocumentRecord.value().toPdlDokument()
                val pdlPerson = pdlDokument.mapToInternal()
                pdlPersonService.oppdaterPdlPerson(pdlPerson)
                secureLogger.debug("Mottok pdlPerson ${pdlPerson}")
            }
        }.onFailure {
            secureLogger.error("Failed to process pdldokument record", it)
            throw RuntimeException("Could not process pdldokument record. See more details in secure log.")
        }
    }

    private fun String.toPdlDokument(): PdlDokument = mapper.readValue(this, PdlDokument::class.java)

    private fun PdlDokument.mapToInternal(): PdlPerson {
        val ikkeHistoriskeFnr =
            this.hentIdenter.identer.filter { it.gruppe == IdentGruppeDto.FOLKEREGISTERIDENT }.filter { !it.historisk }
        if (ikkeHistoriskeFnr.count() == 0 || ikkeHistoriskeFnr.count() > 1) {
            logger.debug("Fant ${ikkeHistoriskeFnr.count()} potensielle fnr")
        }
        val foedslsnr = ikkeHistoriskeFnr.firstOrNull()

        val ikkeHistoriskeNavn = this.hentPerson.navn.filter { !it.metadata.historisk }
        if (ikkeHistoriskeNavn.count() == 0 || ikkeHistoriskeNavn.count() > 1) {
            logger.debug("Fant ${ikkeHistoriskeNavn.count()} potensielle navn")
        }
        val navn = ikkeHistoriskeNavn.firstOrNull()

        val ikkeHistoriskeAdressebeskyttelser =
            this.hentPerson.adressebeskyttelse.filter { it.metadata.historisk == false }
        if (ikkeHistoriskeAdressebeskyttelser.count() > 1) {
            logger.debug("Fant ${ikkeHistoriskeAdressebeskyttelser.count()} potensielle adressebeskyttelser")
        }
        val adressebeskyttelse = ikkeHistoriskeAdressebeskyttelser.firstOrNull()
        if (foedslsnr == null || navn == null) {
            secureLogger.warn("Foedselsnr or navn is missing, cannot proceed with $this")
        }
        return PdlPerson(
            foedslsnr!!.ident,
            navn!!.fornavn,
            navn.mellomnavn,
            navn.etternavn,
            adressebeskyttelse?.let { it.gradering.mapToInternal() })
    }

    private fun GraderingDto.mapToInternal(): Beskyttelsesbehov? =
        when (this) {
            GraderingDto.FORTROLIG -> Beskyttelsesbehov.FORTROLIG
            GraderingDto.STRENGT_FORTROLIG -> Beskyttelsesbehov.STRENGT_FORTROLIG
            GraderingDto.STRENGT_FORTROLIG_UTLAND -> Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND
            else -> null
        }

}




