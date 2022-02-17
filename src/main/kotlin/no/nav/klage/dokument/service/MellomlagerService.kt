package no.nav.klage.dokument.service


import no.nav.klage.dokument.clients.klagefileapi.FileApiClient
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.dokument.domain.OpplastetMellomlagretDokument
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MellomlagerService(
    private val fileApiClient: FileApiClient
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val standardMediaTypeInGCS = MediaType.valueOf("application/pdf")
    }

    fun uploadDocument(dokument: MellomlagretDokument): String =
        fileApiClient.uploadDocument(
            dokument.content,
            dokument.title
        )

    fun uploadByteArray(tittel: String, content: ByteArray): String =
        fileApiClient.uploadDocument(
            content,
            tittel,
        )

    fun uploadMultipartFile(file: MultipartFile): String =
        fileApiClient.uploadDocument(
            file.bytes,
            file.originalFilename ?: throw RuntimeException("missing original filename")
        )

    fun getUploadedDocument(mellomlagerId: String): MellomlagretDokument =
        OpplastetMellomlagretDokument(
            getFileNameFromMellomlagerId(mellomlagerId),
            fileApiClient.getDocument(mellomlagerId),
            standardMediaTypeInGCS
        )

    fun deleteDocument(mellomlagerId: String): Unit =
        fileApiClient.deleteDocument(mellomlagerId)

    fun getUploadedDocumentAsSystemUser(mellomlagerId: String): MellomlagretDokument =
        OpplastetMellomlagretDokument(
            getFileNameFromMellomlagerId(mellomlagerId),
            fileApiClient.getDocument(mellomlagerId, true),
            standardMediaTypeInGCS
        )

    fun deleteDocumentAsSystemUser(mellomlagerId: String): Unit =
        fileApiClient.deleteDocument(mellomlagerId, true)

    fun uploadDocumentAsSystemUser(file: MultipartFile): String =
        fileApiClient.uploadDocument(file.bytes, file.name, true)

    fun updateTittel(mellomlagerId: String, title: String) : String {
        //TODO: Hvordan oppdaterer man egentlig noe i fillageret? Må det gamle slettes?
        val mellomlagretDokument = getUploadedDocument(mellomlagerId)
        return uploadByteArray(title, mellomlagretDokument.content)
    }

    private fun getFileNameFromMellomlagerId(mellomlagerId: String): String = mellomlagerId.substring(36)

}