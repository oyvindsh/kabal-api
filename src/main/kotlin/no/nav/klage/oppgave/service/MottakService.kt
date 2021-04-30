package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.api.view.OversendtKlage
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.domain.klage.KLAGEENHET_PREFIX
import no.nav.klage.oppgave.domain.kodeverk.LovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.LovligeTyper
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import no.nav.klage.oppgave.events.MottakLagretEvent
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.exceptions.OversendtKlageNotValidException
import no.nav.klage.oppgave.repositories.EnhetRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.getLogger
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MottakService(
    environment: Environment,
    private val mottakRepository: MottakRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val dokumentService: DokumentService,
    private val norg2Client: Norg2Client,
    private val enhetRepository: EnhetRepository
) {

    private val lovligeTemaerIKabal = LovligeTemaer.lovligeTemaer(environment)
    private val lovligeTyperIKabal = LovligeTyper.lovligeTyper(environment)


    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @Transactional
    fun createMottakForKlage(oversendtKlage: OversendtKlage) {
        oversendtKlage.validate()
        val mottak = mottakRepository.save(oversendtKlage.toMottak())
        logger.debug("Har lagret mottak {}, publiserer nå event", mottak.id)
        applicationEventPublisher.publishEvent(MottakLagretEvent(mottak))
    }

    fun OversendtKlage.validate() {
        tilknyttedeJournalposter.forEach { validateJournalpost(it.journalpostId) }

        validateTema(tema)
        validateType(type)
        validateEnhet(avsenderEnhet)
        validateSaksbehandler(avsenderSaksbehandlerIdent, avsenderEnhet)
        oversendtEnhet?.let {
            validateEnhet(it)
            validateKaEnhet(it)
        }
    }

    private fun validateType(type: Type) {
        if (!lovligeTyperIKabal.contains(type)) {
            throw OversendtKlageNotValidException("Kabal kan ikke motta klager med type $type ennå")
        }
    }

    private fun validateTema(tema: Tema) {
        if (!lovligeTemaerIKabal.contains(tema)) {
            throw OversendtKlageNotValidException("Kabal kan ikke motta klager med tema $tema ennå")
        }
    }

    private fun validateSaksbehandler(saksbehandlerident: String, enhet: String) {
        if (enhetRepository.getAnsatteIEnhet(enhet).none { it == saksbehandlerident }) {
            throw OversendtKlageNotValidException("$saksbehandlerident er ikke saksbehandler i enhet $enhet")
        }
    }

    private fun validateKaEnhet(enhet: String) {
        if (!enhet.startsWith(KLAGEENHET_PREFIX)) {
            logger.warn("{} is not a klageinstansen enhet", enhet)
            throw OversendtKlageNotValidException("$enhet er ikke en enhet i klageinstansen")
        }
    }

    private fun validateEnhet(enhet: String) =
        try {
            norg2Client.fetchEnhet(enhet).navn
        } catch (e: RuntimeException) {
            logger.warn("Unable to validate enhet from oversendt klage: {}", enhet, e)
            throw OversendtKlageNotValidException("$enhet er ikke en gyldig NAV-enhet")
        }

    private fun validateJournalpost(journalpostId: String) =
        try {
            dokumentService.validateJournalpostExistsAsSystembruker(journalpostId)
        } catch (e: JournalpostNotFoundException) {
            logger.warn("Unable to validate journalpost from oversendt klage: {}", journalpostId, e)
            throw OversendtKlageNotValidException("$journalpostId er ikke en gyldig journalpost referanse")
        }
    
}
