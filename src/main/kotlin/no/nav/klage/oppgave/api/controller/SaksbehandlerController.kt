package no.nav.klage.oppgave.api.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import no.nav.klage.oppgave.api.view.Enhet
import no.nav.klage.oppgave.api.view.Medunderskriver
import no.nav.klage.oppgave.api.view.Medunderskrivere
import no.nav.klage.oppgave.config.SecurityConfiguration
import no.nav.klage.oppgave.domain.EnheterMedLovligeTemaer
import no.nav.klage.oppgave.domain.kodeverk.Tema
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.service.SaksbehandlerService
import no.nav.klage.oppgave.util.getLogger
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Api(tags = ["kabal-api"])
@ProtectedWithClaims(issuer = SecurityConfiguration.ISSUER_AAD)
class SaksbehandlerController(
    private val saksbehandlerService: SaksbehandlerService,
    private val saksbehandlerRepository: SaksbehandlerRepository
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @ApiOperation(
        value = "Hent klageenheter for en ansatt",
        notes = "Henter alle klageenheter som saksbehandler er knyttet til."
    )
    @GetMapping("/ansatte/{navIdent}/enheter", produces = ["application/json"])
    fun getEnheter(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String
    ): List<Enhet> {
        logger.debug("getEnheter is requested by $navIdent")
        val enheter = saksbehandlerService.getEnheterMedTemaerForSaksbehandler().toEnheter()
        logEnheter(enheter, navIdent)
        return enheter
    }

    @ApiOperation(
        value = "Hent medunderskriver for en ansatt",
        notes = "Henter alle medunderskrivere som saksbehandler er knyttet til for et gitt tema."
    )
    @GetMapping("/ansatte/{navIdent}/medunderskrivere/{tema}", produces = ["application/json"])
    fun getMedunderskrivere(
        @ApiParam(value = "NavIdent til en ansatt")
        @PathVariable navIdent: String,
        @ApiParam(value = "Tema man trenger medunderskrivere for")
        @PathVariable tema: String
    ): Medunderskrivere {
        logger.debug("getMedunderskrivere is requested by $navIdent")
        val medunderskrivere = saksbehandlerRepository.getAlleSaksbehandlerIdenter()
            .filter { it != navIdent }
            .filter { saksbehandlerHarTilgangTilTema(it, tema) }
            .map { Medunderskriver(it, getNameForIdent(it)) }
        return Medunderskrivere(tema, medunderskrivere)
    }

    private fun saksbehandlerHarTilgangTilTema(ident: String, tema: String) =
        saksbehandlerRepository.getEnheterMedTemaerForSaksbehandler(ident).enheter.flatMap { it.temaer }
            .contains(Tema.of(tema))

    private fun getNameForIdent(it: String) =
        saksbehandlerRepository.getNamesForSaksbehandlere(setOf(it)).getOrDefault(it, "Ukjent navn")

    private fun logEnheter(enheter: List<Enhet>, navIdent: String) {
        enheter.forEach { enhet ->
            logger.debug(
                "{} has access to {} ({}) with temaer {}",
                navIdent,
                enhet.id,
                enhet.navn,
                enhet.lovligeTemaer.joinToString(separator = ",")
            )
        }
    }

    private fun EnheterMedLovligeTemaer.toEnheter() =
        this.enheter.map { enhet ->
            Enhet(
                id = enhet.enhetId,
                navn = enhet.navn,
                lovligeTemaer = enhet.temaer.map { tema -> tema.id }
            )
        }
}

