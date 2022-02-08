package no.nav.klage.dokument.domain

import no.nav.klage.oppgave.util.getLogger
import org.springframework.web.multipart.MultipartFile
import java.io.*

class MellomlagretMultipartFile(private val mellomlagretDokument: MellomlagretDokument) : MultipartFile {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun getName(): String {
        return mellomlagretDokument.title
    }

    override fun getOriginalFilename(): String? {
        return mellomlagretDokument.title
    }

    override fun getContentType(): String? {
        return mellomlagretDokument.contentType.toString()
    }

    override fun isEmpty(): Boolean {
        return mellomlagretDokument.content.isEmpty()
    }

    override fun getSize(): Long {
        return mellomlagretDokument.content.size.toLong()
    }

    @Throws(IOException::class)
    override fun getBytes(): ByteArray {
        return mellomlagretDokument.content
    }

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(mellomlagretDokument.content)
    }

    @Throws(IOException::class)
    override fun transferTo(dest: File) {
        FileOutputStream(dest).use { f -> f.write(mellomlagretDokument.content) }
    }
}