package no.nav.klage.oppgave.api.controller

import io.swagger.v3.oas.annotations.tags.Tag
import no.nav.klage.kodeverk.Fagsystem
import no.nav.klage.kodeverk.Type
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.api.view.kabin.*
import no.nav.klage.oppgave.config.SecurityConfiguration.Companion.ISSUER_AAD
import no.nav.klage.oppgave.service.*
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.logMethodDetails
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Tag(name = "kabal-api")
@ProtectedWithClaims(issuer = ISSUER_AAD)
@RequestMapping("/api/internal")
class KabinApiController(
    private val klagebehandlingService: KlagebehandlingService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val mottakService: MottakService,
    private val fullmektigSearchService: FullmektigSearchService,
    private val kabinApiService: KabinApiService
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    @PostMapping("/isduplicate")
    fun isDuplicate(
        @RequestBody input: IsDuplicateInput
    ): Boolean {
        logMethodDetails(
            methodName = ::isDuplicate.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )
        return mottakService.isDuplicate(
            fagsystem = KildeFagsystem.valueOf(
                Fagsystem.of(input.fagsystemId).name
            ),
            kildeReferanse = input.kildereferanse,
            type = Type.of(input.typeId)
        )
    }

    @PostMapping("/searchpart")
    fun searchPart(
        @RequestBody input: IdentifikatorInput
    ): BehandlingDetaljerView.PartView {
        logMethodDetails(
            methodName = ::searchPart.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )
        return fullmektigSearchService.searchFullmektig(
            identifikator = input.identifikator
        )
    }

    @PostMapping("/completedklagebehandlinger")
    fun getCompletedKlagebehandlinger(
        @RequestBody input: GetCompletedKlagebehandlingerInput
    ): List<CompletedKlagebehandling> {
        logMethodDetails(
            methodName = ::getCompletedKlagebehandlinger.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return klagebehandlingService.findCompletedKlagebehandlingerByPartIdValue(
            partIdValue = input.idnummer
        )
    }

    @GetMapping("/completedklagebehandlinger/{klagebehandlingId}")
    fun getCompletedKlagebehandling(
        @PathVariable klagebehandlingId: UUID
    ): CompletedKlagebehandling {
        logMethodDetails(
            methodName = ::getCompletedKlagebehandling.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return klagebehandlingService.findCompletedKlagebehandlingById(
            klagebehandlingId = klagebehandlingId
        )
    }

    @PostMapping("/createanke")
    fun createAnke(
        @RequestBody input: CreateAnkeBasedOnKabinInput
    ): CreatedAnkeResponse {
        logMethodDetails(
            methodName = ::createAnke.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return kabinApiService.createAnke(
            input = input
        )
    }

    @GetMapping("/anker/{mottakId}/status")
    fun getCreatedAnkebehandlingStatus(
        @PathVariable mottakId: UUID
    ): CreatedAnkebehandlingStatusForKabin {
        logMethodDetails(
            methodName = ::getCreatedAnkebehandlingStatus.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return kabinApiService.getCreatedAnkebehandlingStatus(
            mottakId = mottakId
        )
    }

    @PostMapping("/searchusedjournalpostid")
    fun getUsedJournalpostIdListForPerson(
        @RequestBody input: SearchUsedJournalpostIdInput,
    ): List<String> {
        logMethodDetails(
            methodName = ::getUsedJournalpostIdListForPerson.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return kabinApiService.getUsedJournalpostIdListForPerson(
            input = input
        )
    }

    @PostMapping("/createklage")
    fun createKlage(
        @RequestBody input: CreateKlageBasedOnKabinInput
    ): CreatedKlageResponse {
        logMethodDetails(
            methodName = ::createKlage.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return kabinApiService.createKlage(
            input = input
        )
    }

    @GetMapping("/klager/{mottakId}/status")
    fun getCreatedKlagebehandlingStatus(
        @PathVariable mottakId: UUID
    ): CreatedKlagebehandlingStatusForKabin {
        logMethodDetails(
            methodName = ::getCreatedKlagebehandlingStatus.name,
            innloggetIdent = innloggetSaksbehandlerService.getInnloggetIdent(),
            logger = logger
        )

        return kabinApiService.getCreatedKlagebehandlingStatus(
            mottakId = mottakId
        )
    }
}