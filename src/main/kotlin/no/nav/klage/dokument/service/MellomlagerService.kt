package no.nav.klage.dokument.service


import no.nav.klage.dokument.clients.klagefileapi.FileApiClient
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.oppgave.util.Image2PDF
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

@Service
class MellomlagerService(
    private val fileApiClient: FileApiClient,
    private val image2PDF: Image2PDF,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val standardMediaTypeInGCS = MediaType.valueOf("application/pdf")
    }

    fun uploadDocument(dokument: MellomlagretDokument): String {
        return fileApiClient.uploadDocument(
            //If uploaded file is an image, convert to pdf
            bytes = image2PDF.convertIfImage(dokument.content),
            originalFilename = dokument.title
        )
    }

    fun uploadByteArray(tittel: String, content: ByteArray): String =
        fileApiClient.uploadDocument(
            bytes = content,
            originalFilename = tittel,
        )

    fun getUploadedDocument(mellomlagerId: String): ByteArray =
        fileApiClient.getDocument(mellomlagerId)

    fun deleteDocument(mellomlagerId: String): Unit =
        fileApiClient.deleteDocument(mellomlagerId)

}