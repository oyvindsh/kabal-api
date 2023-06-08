package no.nav.klage.dokument.service

import no.nav.klage.dokument.clients.clamav.ClamAvClient
import no.nav.klage.dokument.domain.FysiskDokument
import no.nav.klage.dokument.exceptions.AttachmentEncryptedException
import no.nav.klage.dokument.exceptions.AttachmentHasVirusException
import no.nav.klage.dokument.exceptions.AttachmentIsEmptyException
import no.nav.klage.dokument.exceptions.AttachmentTooLargeException
import no.nav.klage.oppgave.util.getLogger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.tika.Tika
import org.springframework.http.MediaType
import org.springframework.util.unit.DataSize

class MellomlagretDokumentValidatorService(
    private val clamAvClient: ClamAvClient,
    private val maxAttachmentSize: DataSize
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun validateAttachment(fil: FysiskDokument) {
        logger.debug("Validating attachment.")
        if (fil.content.isEmpty()) {
            logger.warn("Attachment is empty")
            throw AttachmentIsEmptyException()
        }

        if (fil.isTooLarge()) {
            logger.warn("Attachment too large")
            throw AttachmentTooLargeException()
        }

        if (fil.hasVirus()) {
            logger.warn("Attachment has virus")
            throw AttachmentHasVirusException()
        }

        if (fil.isPDF() && fil.isEncrypted()) {
            logger.warn("Attachment is encrypted")
            throw AttachmentEncryptedException()
        }

        logger.debug("Validation successful.")
    }

    private fun FysiskDokument.hasVirus() = !clamAvClient.scan(this.content)

    private fun FysiskDokument.isEncrypted(): Boolean {
        return try {
            val temp: PDDocument = PDDocument.load(this.content)
            temp.close()
            false
        } catch (ipe: InvalidPasswordException) {
            true
        }
    }

    private fun FysiskDokument.isTooLarge() = this.content.size > maxAttachmentSize.toBytes()

    private fun FysiskDokument.isPDF() =
        MediaType.valueOf(Tika().detect(this.content)) == MediaType.APPLICATION_PDF
}