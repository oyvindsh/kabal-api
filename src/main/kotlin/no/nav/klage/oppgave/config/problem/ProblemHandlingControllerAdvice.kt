package no.nav.klage.oppgave.config.problem

import no.nav.klage.oppgave.exceptions.OppgaveIdWrongFormatException
import no.nav.klage.oppgave.exceptions.OppgaveNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.Status
import org.zalando.problem.spring.web.advice.AdviceTrait
import org.zalando.problem.spring.web.advice.ProblemHandling

@ControllerAdvice
class ProblemHandlingControllerAdvice : OppgaveNotFoundExceptionAdviceTrait, ProblemHandling

interface OppgaveNotFoundExceptionAdviceTrait : AdviceTrait {

    @ExceptionHandler
    fun handleOppgaveNotFound(ex: OppgaveNotFoundException, request: NativeWebRequest): ResponseEntity<Problem> =
        create(Status.NOT_FOUND, ex, request)

    @ExceptionHandler
    fun handleOppgaveIdNotParsable(ex: OppgaveIdWrongFormatException, request: NativeWebRequest): ResponseEntity<Problem> =
        create(Status.BAD_REQUEST, ex, request)

}