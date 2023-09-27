package no.nav.klage.dokument.service

import no.nav.klage.dokument.api.mapper.DokumentMapper
import no.nav.klage.dokument.clients.kabaljsontopdf.InnholdsfortegnelseRequest
import no.nav.klage.dokument.clients.kabaljsontopdf.KabalJsonToPdfClient
import no.nav.klage.dokument.domain.dokumenterunderarbeid.Innholdsfortegnelse
import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.dokument.repositories.InnholdsfortegnelseRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class InnholdsfortegnelseService(
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val dokumentMapper: DokumentMapper,
    private val mellomlagerService: MellomlagerService,
    private val kabalJsonToPdfClient: KabalJsonToPdfClient,
    private val innholdsfortegnelseRepository: InnholdsfortegnelseRepository,

    ) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun saveInnholdsfortegnelse(dokumentUnderArbeidId: UUID, mottakere: List<String>) {
        logger.debug("Received saveInnholdsfortegnelse")

        val content = getInnholdsfortegnelseAsPdf(dokumentUnderArbeidId = dokumentUnderArbeidId, mottakere = mottakere)

        val mellomlagerId =
            mellomlagerService.uploadByteArray(
                tittel = "Innholdsfortegnelse",
                content = content,
            )

        innholdsfortegnelseRepository.save(
            Innholdsfortegnelse(
                mellomlagerId = mellomlagerId,
                hoveddokumentId = dokumentUnderArbeidId,
                created = LocalDateTime.now(),
                modified = LocalDateTime.now(),
            )
        )
    }

    fun getInnholdsfortegnelseAsPdf(dokumentUnderArbeidId: UUID, mottakere: List<String> = emptyList()): ByteArray {
        logger.debug("Received getInnholdsfortegnelseAsPdf")

        val document = dokumentUnderArbeidRepository.getReferenceById(dokumentUnderArbeidId)

        if (document.parentId != null) {
            throw IllegalArgumentException("must be hoveddokument")
        }

        val vedlegg = dokumentUnderArbeidRepository.findByParentIdOrderByCreated(dokumentUnderArbeidId)

        val documentList = dokumentMapper.getSortedDokumentViewListForInnholdsfortegnelse(vedlegg.toList(), mottakere)

        //TODO implement
        val pdfDocument =
            kabalJsonToPdfClient.getInnholdsfortegnelse(InnholdsfortegnelseRequest(documentList = documentList))

        return pdfDocument.bytes
    }
}