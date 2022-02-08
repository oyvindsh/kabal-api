package no.nav.klage.dokument.domain

import org.springframework.http.MediaType

data class OpplastetMellomlagretDokument(
    override val title: String,
    override val content: ByteArray,
    override val contentType: MediaType
) : MellomlagretDokument {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OpplastetMellomlagretDokument

        if (title != other.title) return false
        if (!content.contentEquals(other.content)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + content.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}