package no.nav.klage.oppgave.util

import org.springframework.stereotype.Component
import org.verapdf.pdfa.Foundries
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider
import java.io.ByteArrayInputStream

@Component
class PdfUtils {
    init {
        VeraGreenfieldFoundryProvider.initialise()
    }

    fun pdfByteArrayIsPdfa(byteArray: ByteArray): Boolean {
        val parser = Foundries.defaultInstance().createParser(ByteArrayInputStream(byteArray))
        val validator = Foundries.defaultInstance().createValidator(parser.flavour, false)
        val result = validator.validate(parser)
        return result.isCompliant
    }
}