package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Melding
import no.nav.klage.oppgave.exceptions.MeldingNotFoundException
import no.nav.klage.oppgave.repositories.MeldingRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import javax.persistence.EntityNotFoundException

@Service
@Transactional
class MeldingService(
    private val meldingRepository: MeldingRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    fun addMelding(
        klagebehandlingId: UUID,
        innloggetIdent: String,
        text: String
    ): Melding {
        logger.debug("saving new melding by $innloggetIdent")
        return meldingRepository.save(
            Melding(
                text = text,
                klagebehandlingId = klagebehandlingId,
                saksbehandlerident = innloggetIdent,
                created = LocalDateTime.now()
            )
        )
    }

    fun deleteMelding(
        klagebehandlingId: UUID,
        innloggetIdent: String,
        meldingId: UUID
    ) {
        try {
            val melding = meldingRepository.getOne(meldingId)
            if (melding.saksbehandlerident == innloggetIdent) {
                meldingRepository.delete(melding)
                logger.debug("melding ($meldingId) deleted by $innloggetIdent")
            } else {
                throw RuntimeException(
                    "saksbehandler ($innloggetIdent) is not the author of melding ($meldingId), and is not allowed to delete it."
                )
            }
        } catch (enfe: EntityNotFoundException) {
            throw MeldingNotFoundException("couldn't find melding with id $meldingId")
        }

    }

    fun modifyMelding(
        klagebehandlingId: UUID,
        innloggetIdent: String,
        meldingId: UUID,
        text: String
    ): Melding {
        try {
            val melding = meldingRepository.getOne(meldingId)
            if (melding.saksbehandlerident == innloggetIdent) {
                melding.text = text
                melding.modified = LocalDateTime.now()

                meldingRepository.save(melding)
                logger.debug("melding ($meldingId) modified by $innloggetIdent")

                return melding
            } else {
                throw RuntimeException(
                    "saksbehandler ($innloggetIdent) is not the author of melding ($meldingId), and is not allowed to modify it."
                )
            }
        } catch (enfe: EntityNotFoundException) {
            throw MeldingNotFoundException("couldn't find melding with id $meldingId")
        }
    }

    fun getMeldingerForKlagebehandling(klagebehandlingId: UUID) =
        meldingRepository.findByKlagebehandlingIdOrderByCreatedDesc(klagebehandlingId)

}