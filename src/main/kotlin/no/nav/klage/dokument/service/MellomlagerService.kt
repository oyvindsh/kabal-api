package no.nav.klage.dokument.service


import no.nav.klage.dokument.clients.klagefileapi.FileApiClient
import no.nav.klage.dokument.domain.MellomlagretDokument
import no.nav.klage.oppgave.util.getLogger
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

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

    fun getUploadedDocument(mellomlagerId: String): ByteArray =
        fileApiClient.getDocument(mellomlagerId)

    fun deleteDocument(mellomlagerId: String): Unit =
        fileApiClient.deleteDocument(mellomlagerId)

}