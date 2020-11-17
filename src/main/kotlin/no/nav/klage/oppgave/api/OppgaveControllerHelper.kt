package no.nav.klage.oppgave.api

import no.nav.klage.oppgave.exceptions.NotMatchingUserException
import no.nav.klage.oppgave.exceptions.OppgaveIdWrongFormatException
import no.nav.klage.oppgave.exceptions.OppgaveVersjonWrongFormatException
import no.nav.klage.oppgave.repositories.InnloggetSaksbehandlerRepository
import org.springframework.stereotype.Service

@Service
class OppgaveControllerHelper(private val innloggetSaksbehandlerRepository: InnloggetSaksbehandlerRepository) {

    fun validateNavIdent(navIdent: String) {
        val innloggetIdent = innloggetSaksbehandlerRepository.getInnloggetIdent()
        if (innloggetIdent != navIdent) {
            throw NotMatchingUserException(
                "logged in user does not match sent in user. " +
                        "Logged in: $innloggetIdent, sent in: $navIdent"
            )
        }
    }

    fun toLongOrException(oppgaveId: String?) =
        oppgaveId?.toLongOrNull() ?: throw OppgaveIdWrongFormatException("OppgaveId could not be parsed as a Long")

    fun toIntOrException(versjon: String?) =
        versjon?.toIntOrNull()
            ?: throw OppgaveVersjonWrongFormatException("Oppgaveversjon could not be parsed as an Int")

}