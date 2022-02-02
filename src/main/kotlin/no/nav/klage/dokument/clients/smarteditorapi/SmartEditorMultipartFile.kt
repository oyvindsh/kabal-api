package no.nav.klage.dokument.clients.smarteditorapi

import no.nav.klage.oppgave.util.getLogger
import org.springframework.web.multipart.MultipartFile
import java.io.*

class SmartEditorMultipartFile(
    private val content: ByteArray,
    private val fileName: String,
) : MultipartFile {

    companion object {
        private val logger = getLogger(javaClass.enclosingClass)
    }

    override fun getName(): String {
        return fileName
    }

    override fun getOriginalFilename(): String? {
        return fileName
    }

    override fun getContentType(): String? {
        return "application/pdf"
    }

    override fun isEmpty(): Boolean {
        return content.isEmpty()
    }

    override fun getSize(): Long {
        return content.size.toLong()
    }

    @Throws(IOException::class)
    override fun getBytes(): ByteArray {
        return content
    }

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(content)
    }

    @Throws(IOException::class)
    override fun transferTo(dest: File) {
        FileOutputStream(dest).use { f -> f.write(content) }
    }
}