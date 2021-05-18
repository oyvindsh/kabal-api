package no.nav.klage.oppgave.api.controller

import no.nav.klage.oppgave.api.view.KodeverkResponse
import no.nav.klage.oppgave.api.view.toDto
import no.nav.klage.oppgave.domain.kodeverk.LovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.LovligeTyper
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.domain.kodeverk.Type
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class KodeverkController(private val environment: Environment) {

    private val lovligeTemaerIKabal = LovligeTemaer.lovligeTemaer(environment)
    private val lovligeTyperIKabal = LovligeTyper.lovligeTyper(environment)

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @GetMapping("/kodeverk", produces = ["application/json"])
    fun getKodeverk(): KodeverkResponse {

        return KodeverkResponse(
            tema = Tema.values().asList().filter { lovligeTemaerIKabal.contains(it) }.toDto(),
            type = Type.values().asList().filter { lovligeTyperIKabal.contains(it) }.toDto()
        )
    }
}