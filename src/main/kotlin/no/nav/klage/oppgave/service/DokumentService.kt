package no.nav.klage.oppgave.service

import no.nav.klage.dokument.api.view.JournalfoertDokumentReference
import no.nav.klage.dokument.domain.FysiskDokument
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.Tema
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.saf.graphql.*
import no.nav.klage.oppgave.clients.saf.rest.SafRestClient
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.repositories.MergedDocumentRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentInformation
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import kotlin.system.measureTimeMillis


@Service
@Transactional
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafRestClient,
    private val safClient: SafGraphQlClient,
    private val mergedDocumentRepository: MergedDocumentRepository,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val dokumentMapper = DokumentMapper()
    }

    fun fetchDokumentlisteForBehandling(
        behandling: Behandling,
        temaer: List<Tema>,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        if (behandling.sakenGjelder.erPerson()) {
            val dokumentoversiktBruker: DokumentoversiktBruker =
                safGraphQlClient.getDokumentoversiktBruker(
                    behandling.sakenGjelder.partId.value,
                    mapTema(temaer),
                    pageSize,
                    previousPageRef
                )

            //Attempt to track down elusive bug with repeated documents
            val uniqueJournalposter = dokumentoversiktBruker.journalposter.map { it.journalpostId }.toSet()
            if (uniqueJournalposter.size != dokumentoversiktBruker.journalposter.size) {
                secureLogger.error(
                    "Received list of non unique documents from SAF.\nUnique list: ${
                        uniqueJournalposter.joinToString()
                    }. " + "\nFull list: ${
                        dokumentoversiktBruker.journalposter.joinToString { it.journalpostId }
                    }" + "\nParams: fnr: ${behandling.sakenGjelder.partId.value}, behandlingId: ${behandling.id}, " +
                            "temaer: ${temaer.joinToString()}, pageSize: $pageSize, previousPageRef: $previousPageRef"
                )
            }

            return DokumenterResponse(
                dokumenter = dokumentoversiktBruker.journalposter.map { journalpost ->
                    dokumentMapper.mapJournalpostToDokumentReferanse(journalpost, behandling)
                },
                pageReference = if (dokumentoversiktBruker.sideInfo.finnesNesteSide) {
                    dokumentoversiktBruker.sideInfo.sluttpeker
                } else {
                    null
                },
                antall = dokumentoversiktBruker.sideInfo.antall,
                totaltAntall = dokumentoversiktBruker.sideInfo.totaltAntall
            )
        } else {
            return DokumenterResponse(dokumenter = emptyList(), pageReference = null, antall = 0, totaltAntall = 0)
        }
    }

    fun fetchJournalpostIdList(
        behandling: Behandling,
        pageSize: Int,
        previousPageRef: String?
    ): JournalpostIdListResponse {
        if (behandling.sakenGjelder.erPerson()) {
            val simpleJournalpostListForBruker: SimpleJournalpostListForBruker =
                safGraphQlClient.getJournalpostIdListForBruker(
                    behandling.sakenGjelder.partId.value,
                    pageSize,
                    previousPageRef
                )

            val journalpostList = simpleJournalpostListForBruker.journalposter.map {
                mapSimpleJournalpostToJournalpostReference(it)
            }

            val vedleggCount = journalpostList.sumOf { it.vedlegg.size }

            return JournalpostIdListResponse(
                journalpostList = journalpostList.map {
                    SimpleJournalpostReference(
                        journalpostId = it.journalpostId,
                        dokumentInfoId = it.dokumentInfoId,
                        vedlegg = it.vedlegg
                    )
                },
                journalpostCount = journalpostList.size,
                vedleggCount = vedleggCount,
                sakList = journalpostList.mapNotNull { it.sak }.toSet().toList(),
                avsenderMottakerList = journalpostList.mapNotNull { it.avsenderMottaker }.toSet().toList(),
                temaIdList = journalpostList.mapNotNull { it.temaId }.toSet().toList(),
                journalposttypeList = journalpostList.mapNotNull { it.journalposttype }.toSet().toList(),
                fromDate = journalpostList.minOf { it.registrert },
                toDate = journalpostList.maxOf { it.registrert },
            )
        } else {
            return JournalpostIdListResponse(
                journalpostList = listOf(),
                journalpostCount = 0,
                vedleggCount = 0,
                sakList = listOf(),
                avsenderMottakerList = listOf(),
                temaIdList = listOf(),
                journalposttypeList = listOf(),
                fromDate = null,
                toDate = null,
            )
        }
    }

    fun mapSimpleJournalpostToJournalpostReference(
        journalpost: SimpleJournalpost,
    ): JournalpostReference {

        val hoveddokument = journalpost.dokumenter?.firstOrNull()
            ?: throw RuntimeException("Could not find hoveddokument for journalpost ${journalpost.journalpostId}")

        val journalpostReference = JournalpostReference(
            dokumentInfoId = hoveddokument.dokumentInfoId,
            journalpostId = journalpost.journalpostId,
            registrert = journalpost.datoOpprettet.toLocalDate(),
            journalposttype = DokumentReferanse.Journalposttype.valueOf(journalpost.journalposttype!!.name),

            sak = if (journalpost.sak != null) {
                DokumentReferanse.Sak(
                    datoOpprettet = journalpost.sak.datoOpprettet,
                    fagsakId = journalpost.sak.fagsakId,
                    fagsaksystem = journalpost.sak.fagsaksystem,
                    fagsystemId = journalpost.sak.fagsaksystem?.let { Fagsystem.fromNavn(it).id }
                )
            } else null,
            avsenderMottaker = if (journalpost.avsenderMottaker == null ||
                (journalpost.avsenderMottaker.id == null ||
                        journalpost.avsenderMottaker.type == null)
            ) {
                null
            } else {
                DokumentReferanse.AvsenderMottaker(
                    id = journalpost.avsenderMottaker.id,
                    type = DokumentReferanse.AvsenderMottaker.AvsenderMottakerIdType.valueOf(
                        journalpost.avsenderMottaker.type.name
                    ),
                    navn = journalpost.avsenderMottaker.navn,
                )
            },
            temaId = Tema.fromNavn(journalpost.tema?.name).id,
            vedlegg = journalpost.dokumenter.subList(1, journalpost.dokumenter.size).map {
                it.dokumentInfoId
            }
        )

        return journalpostReference
    }

    fun fetchDokumentReferanse(
        journalpostId: String,
    ): DokumentReferanse {
        val journalpost = safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId = journalpostId)
        return dokumentMapper.mapJournalpostToDokumentReferanse(journalpost)
    }

    private fun mapTema(temaer: List<Tema>): List<no.nav.klage.oppgave.clients.saf.graphql.Tema> =
        temaer.map { tema -> no.nav.klage.oppgave.clients.saf.graphql.Tema.valueOf(tema.name) }

    fun fetchJournalposterConnectedToBehandling(behandling: Behandling): DokumenterResponse {
        val dokumentReferanser = behandling.saksdokumenter.groupBy { it.journalpostId }.keys
            .map { safGraphQlClient.getJournalpostAsSaksbehandler(it) }
            .map { dokumentMapper.mapJournalpostToDokumentReferanse(it, behandling) }
        return DokumenterResponse(
            dokumenter = dokumentReferanser.sortedByDescending { it.registrert },
            pageReference = null,
            antall = dokumentReferanser.size,
            totaltAntall = dokumentReferanser.size
        )
    }

    fun validateJournalpostExists(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            throw JournalpostNotFoundException("Journalpost $journalpostId not found")
        }
    }

    fun validateJournalpostExistsAsSystembruker(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpostAsSystembruker(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId: String): List<String> {
        return try {
            val journalpost = safGraphQlClient.getJournalpostAsSystembruker(journalpostId)
            journalpost.dokumenter?.filter { harArkivVariantformat(it) }?.map { it.dokumentInfoId } ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            emptyList()
        }
    }

    fun getFysiskDokument(journalpostId: String, dokumentInfoId: String): FysiskDokument {
        val arkivertDokument = safRestClient.getDokument(dokumentInfoId, journalpostId)

        return FysiskDokument(
            title = getDocumentTitle(journalpostId = journalpostId, dokumentInfoId = dokumentInfoId),
            content = arkivertDokument.bytes,
            contentType = arkivertDokument.contentType,
        )
    }

    fun getDocumentTitle(journalpostId: String, dokumentInfoId: String): String {
        val journalpostInDokarkiv =
            safClient.getJournalpostAsSaksbehandler(journalpostId)

        return journalpostInDokarkiv.dokumenter?.find { it.dokumentInfoId == dokumentInfoId }?.tittel
            ?: throw RuntimeException("Document/title not found in Dokarkiv")
    }

    fun changeTitleInPDF(documentBytes: ByteArray, title: String): ByteArray {
        val baos = ByteArrayOutputStream()
        val timeMillis = measureTimeMillis {
            val document: PDDocument = PDDocument.load(
                documentBytes,
                "",
                null,
                null,
                MemoryUsageSetting.setupMixed(50_000_000)
            )
            val info: PDDocumentInformation = document.documentInformation
            info.title = title
            document.save(baos)
            document.close()
        }
        secureLogger.debug("changeTitleInPDF with title $title took $timeMillis ms")
        return baos.toByteArray()
    }

    fun getDokumentReferanse(journalpostId: String, behandling: Behandling): DokumentReferanse {
        val journalpost = safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId)
        return dokumentMapper.mapJournalpostToDokumentReferanse(journalpost = journalpost, behandling = behandling)
    }

    private fun harArkivVariantformat(dokumentInfo: DokumentInfo): Boolean =
        dokumentInfo.dokumentvarianter.any { dv ->
            dv.variantformat == Variantformat.ARKIV
        }

    fun createSaksdokumenterFromJournalpostIdSet(journalpostIdSet: List<String>): MutableSet<Saksdokument> {
        val saksdokumenter: MutableSet<Saksdokument> = mutableSetOf()
        journalpostIdSet.forEach {
            saksdokumenter.addAll(createSaksdokument(it))
        }
        return saksdokumenter
    }

    private fun createSaksdokument(journalpostId: String) =
        fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId)
            .map { Saksdokument(journalpostId = journalpostId, dokumentInfoId = it) }

    fun storeDocumentsForMerging(documents: List<JournalfoertDokumentReference>): MergedDocument {
        val hash = documents.joinToString(separator = "") { it.journalpostId + it.dokumentInfoId }.toMd5Hash()

        val previouslyMergedDocument = mergedDocumentRepository.findByHash(hash)
        if (previouslyMergedDocument != null) {
            return previouslyMergedDocument
        }

        return mergedDocumentRepository.save(
            MergedDocument(
                title = generateTitleForDocumentsToMerge(documents),
                documentsToMerge = documents.mapIndexed { index, it ->
                    DocumentToMerge(
                        journalpostId = it.journalpostId,
                        dokumentInfoId = it.dokumentInfoId,
                        index = index,
                    )
                }.toSet(),
                hash = hash,
                created = LocalDateTime.now()
            )
        )
    }

    fun String.toMd5Hash(): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(this.toByteArray())).toString(16).padStart(32, '0')
    }

    private fun generateTitleForDocumentsToMerge(documents: List<JournalfoertDokumentReference>): String {
        val numberOfDocumentNamesToShow = 3
        val truncatedMessage = " ... " + (documents.size - numberOfDocumentNamesToShow) + " til"

        return "(${documents.size}) " + documents
            .joinToString(limit = numberOfDocumentNamesToShow, truncated = truncatedMessage) {
                getDocumentTitle(journalpostId = it.journalpostId, dokumentInfoId = it.dokumentInfoId)
            }
    }

    fun mergeDocuments(id: UUID): Pair<Path, String> {
        val mergedDocument = mergedDocumentRepository.getReferenceById(id)
        val documentsToMerge = mergedDocument.documentsToMerge.sortedBy { it.index }

        val merger = PDFMergerUtility()

        val pdDocumentInformation = PDDocumentInformation()
        pdDocumentInformation.title = mergedDocument.title
        merger.destinationDocumentInformation = pdDocumentInformation

        val pathToMergedDocument = Files.createTempFile(null, null)
        pathToMergedDocument.toFile().deleteOnExit()

        merger.destinationFileName = pathToMergedDocument.toString()

        val documentsWithPaths = documentsToMerge.map {
            val tmpFile = Files.createTempFile("", "")
            it to tmpFile
        }

        Flux.fromIterable(documentsWithPaths).flatMapSequential { (document, path) ->
            safRestClient.downloadDocumentAsMono(
                journalpostId = document.journalpostId,
                dokumentInfoId = document.dokumentInfoId,
                pathToFile = path,
            )
        }.collectList().block()

        documentsWithPaths.forEach { (_, path) ->
            merger.addSource(path.toFile())
        }

        //just under 256 MB before using file system
        merger.mergeDocuments(MemoryUsageSetting.setupMixed(250_000_000))

        //clean tmp files that were downloaded from SAF
        try {
            documentsWithPaths.forEach { (_, pathToTmpFile) ->
                pathToTmpFile.toFile().delete()
            }
        } catch (e: Exception) {
            logger.warn("couldn't delete tmp file", e)
        }

        return pathToMergedDocument to mergedDocument.title
    }

    fun getMergedDocument(id: UUID) = mergedDocumentRepository.getReferenceById(id)
}

class DokumentMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Har ikke tatt høyde for skjerming, ref https://confluence.adeo.no/pages/viewpage.action?pageId=320364687
    fun mapJournalpostToDokumentReferanse(
        journalpost: Journalpost,
        behandling: Behandling
    ): DokumentReferanse {

        val hoveddokument = journalpost.dokumenter?.firstOrNull()
            ?: throw RuntimeException("Could not find hoveddokument for journalpost ${journalpost.journalpostId}")

        val dokumentReferanse = DokumentReferanse(
            tittel = hoveddokument.tittel,
            tema = Tema.fromNavn(journalpost.tema?.name).id,
            temaId = Tema.fromNavn(journalpost.tema?.name).id,
            registrert = journalpost.datoOpprettet.toLocalDate(),
            dokumentInfoId = hoveddokument.dokumentInfoId,
            journalpostId = journalpost.journalpostId,
            harTilgangTilArkivvariant = harTilgangTilArkivvariant(hoveddokument),
            valgt = behandling.saksdokumenter.containsDokument(
                journalpost.journalpostId,
                hoveddokument.dokumentInfoId
            ),
            journalposttype = DokumentReferanse.Journalposttype.valueOf(journalpost.journalposttype!!.name),
            journalstatus = if (journalpost.journalstatus != null) {
                DokumentReferanse.Journalstatus.valueOf(journalpost.journalstatus.name)
            } else null,
            sak = if (journalpost.sak != null) {
                DokumentReferanse.Sak(
                    datoOpprettet = journalpost.sak.datoOpprettet,
                    fagsakId = journalpost.sak.fagsakId,
                    fagsaksystem = journalpost.sak.fagsaksystem,
                    fagsystemId = journalpost.sak.fagsaksystem?.let { Fagsystem.fromNavn(it).id }
                )
            } else null,
            avsenderMottaker = if (journalpost.avsenderMottaker == null ||
                (journalpost.avsenderMottaker.id == null ||
                        journalpost.avsenderMottaker.type == null)
            ) {
                null
            } else {
                DokumentReferanse.AvsenderMottaker(
                    id = journalpost.avsenderMottaker.id,
                    type = DokumentReferanse.AvsenderMottaker.AvsenderMottakerIdType.valueOf(
                        journalpost.avsenderMottaker.type.name
                    ),
                    navn = journalpost.avsenderMottaker.navn,
                )
            },
            opprettetAvNavn = journalpost.opprettetAvNavn,
            datoOpprettet = journalpost.datoOpprettet,
            relevanteDatoer = journalpost.relevanteDatoer?.map {
                DokumentReferanse.RelevantDato(
                    dato = it.dato,
                    datotype = DokumentReferanse.RelevantDato.Datotype.valueOf(it.datotype.name)
                )
            },
            kanal = DokumentReferanse.Kanal.valueOf(journalpost.kanal.name),
            kanalnavn = journalpost.kanalnavn,
            utsendingsinfo = getUtsendingsinfo(journalpost.utsendingsinfo),
        )

        dokumentReferanse.vedlegg.addAll(getVedlegg(journalpost, behandling))

        return dokumentReferanse
    }

    fun mapJournalpostToDokumentReferanse(
        journalpost: Journalpost,
    ): DokumentReferanse {

        val hoveddokument = journalpost.dokumenter?.firstOrNull()
            ?: throw RuntimeException("Could not find hoveddokument for journalpost ${journalpost.journalpostId}")

        val dokumentReferanse = DokumentReferanse(
            tittel = hoveddokument.tittel,
            tema = Tema.fromNavn(journalpost.tema?.name).id,
            temaId = Tema.fromNavn(journalpost.tema?.name).id,
            registrert = journalpost.datoOpprettet.toLocalDate(),
            dokumentInfoId = hoveddokument.dokumentInfoId,
            journalpostId = journalpost.journalpostId,
            harTilgangTilArkivvariant = harTilgangTilArkivvariant(hoveddokument),
            valgt = false,
            journalposttype = DokumentReferanse.Journalposttype.valueOf(journalpost.journalposttype!!.name),
            journalstatus = if (journalpost.journalstatus != null) {
                DokumentReferanse.Journalstatus.valueOf(journalpost.journalstatus.name)
            } else null,
            sak = if (journalpost.sak != null) {
                DokumentReferanse.Sak(
                    datoOpprettet = journalpost.sak.datoOpprettet,
                    fagsakId = journalpost.sak.fagsakId,
                    fagsaksystem = journalpost.sak.fagsaksystem,
                    fagsystemId = journalpost.sak.fagsaksystem?.let { Fagsystem.fromNavn(it).id }
                )
            } else null,
            avsenderMottaker = if (journalpost.avsenderMottaker == null ||
                (journalpost.avsenderMottaker.id == null ||
                        journalpost.avsenderMottaker.type == null)
            ) {
                null
            } else {
                DokumentReferanse.AvsenderMottaker(
                    id = journalpost.avsenderMottaker.id,
                    type = DokumentReferanse.AvsenderMottaker.AvsenderMottakerIdType.valueOf(
                        journalpost.avsenderMottaker.type.name
                    ),
                    navn = journalpost.avsenderMottaker.navn,
                )
            },
            opprettetAvNavn = journalpost.opprettetAvNavn,
            datoOpprettet = journalpost.datoOpprettet,
            relevanteDatoer = journalpost.relevanteDatoer?.map {
                DokumentReferanse.RelevantDato(
                    dato = it.dato,
                    datotype = DokumentReferanse.RelevantDato.Datotype.valueOf(it.datotype.name)
                )
            },
            kanal = DokumentReferanse.Kanal.valueOf(journalpost.kanal.name),
            kanalnavn = journalpost.kanalnavn,
            utsendingsinfo = getUtsendingsinfo(journalpost.utsendingsinfo),
        )

        dokumentReferanse.vedlegg.addAll(getVedlegg(journalpost))

        return dokumentReferanse
    }

    private fun getUtsendingsinfo(utsendingsinfo: Utsendingsinfo?): DokumentReferanse.Utsendingsinfo? {
        if (utsendingsinfo == null) {
            return null
        }

        return with(utsendingsinfo) {
            DokumentReferanse.Utsendingsinfo(
                epostVarselSendt = if (epostVarselSendt != null) {
                    DokumentReferanse.Utsendingsinfo.EpostVarselSendt(
                        tittel = epostVarselSendt.tittel,
                        adresse = epostVarselSendt.adresse,
                        varslingstekst = epostVarselSendt.varslingstekst,
                    )
                } else null,
                smsVarselSendt = if (smsVarselSendt != null) {
                    DokumentReferanse.Utsendingsinfo.SmsVarselSendt(
                        adresse = smsVarselSendt.adresse,
                        varslingstekst = smsVarselSendt.varslingstekst,
                    )
                } else null,
                fysiskpostSendt = if (fysiskpostSendt != null) {
                    DokumentReferanse.Utsendingsinfo.FysiskpostSendt(
                        adressetekstKonvolutt = fysiskpostSendt.adressetekstKonvolutt,
                    )
                } else null,
                digitalpostSendt = if (digitalpostSendt != null) {
                    DokumentReferanse.Utsendingsinfo.DigitalpostSendt(
                        adresse = digitalpostSendt.adresse,
                    )
                } else null,
            )
        }
    }

    private fun getVedlegg(
        journalpost: Journalpost,
        klagebehandling: Klagebehandling
    ): List<DokumentReferanse.VedleggReferanse> {
        return if ((journalpost.dokumenter?.size ?: 0) > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                    valgt = klagebehandling.saksdokumenter.containsDokument(
                        journalpost.journalpostId,
                        vedlegg.dokumentInfoId
                    )
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun getVedlegg(
        journalpost: Journalpost,
        behandling: Behandling
    ): List<DokumentReferanse.VedleggReferanse> {
        return if ((journalpost.dokumenter?.size ?: 0) > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                    valgt = behandling.saksdokumenter.containsDokument(
                        journalpost.journalpostId,
                        vedlegg.dokumentInfoId
                    )
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun getVedlegg(
        journalpost: Journalpost,
    ): List<DokumentReferanse.VedleggReferanse> {
        return if ((journalpost.dokumenter?.size ?: 0) > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                    valgt = false,
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun harTilgangTilArkivvariant(dokumentInfo: DokumentInfo?): Boolean =
        dokumentInfo?.dokumentvarianter?.any { dv ->
            dv.variantformat == Variantformat.ARKIV && dv.saksbehandlerHarTilgang
        } == true

    private fun MutableSet<Saksdokument>.containsDokument(journalpostId: String, dokumentInfoId: String) =
        any {
            it.journalpostId == journalpostId && it.dokumentInfoId == dokumentInfoId
        }
}
