package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.oppgave.api.view.BehandlingDetaljerView
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class FullmektigSearchService(
    private val eregClient: EregClient,
    private val pdlFacade: PdlFacade,
    private val behandlingService: BehandlingService,
    private val tilgangService: TilgangService,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun searchFullmektig(identifikator: String, skipAccessControl: Boolean = false): BehandlingDetaljerView.ProsessfullmektigView =
        when (behandlingService.getPartIdFromIdentifikator(identifikator).type) {
            PartIdType.PERSON -> {
                try {
                    if (skipAccessControl || tilgangService.harInnloggetSaksbehandlerTilgangTil(identifikator)) {
                        val person = pdlFacade.getPersonInfo(identifikator)
                        BehandlingDetaljerView.ProsessfullmektigView(
                            person = BehandlingDetaljerView.PersonView(
                                foedselsnummer = person.foedselsnr, navn = BehandlingDetaljerView.NavnView(
                                    fornavn = person.fornavn,
                                    mellomnavn = person.mellomnavn,
                                    etternavn = person.etternavn
                                ), kjoenn = person.kjoenn
                            ),
                            virksomhet = null
                        )
                    } else {
                        secureLogger.warn("Saksbehandler does not have access to view person")
                        BehandlingDetaljerView.ProsessfullmektigView(person = null, virksomhet = null)
                    }
                } catch (e: Exception) {
                    BehandlingDetaljerView.ProsessfullmektigView(person = null, virksomhet = null)
                }
            }
            PartIdType.VIRKSOMHET -> {
                val organisasjon = eregClient.hentOrganisasjon(identifikator)
                if (organisasjon == null) {
                    BehandlingDetaljerView.ProsessfullmektigView(person = null, virksomhet = null)
                } else {
                    BehandlingDetaljerView.ProsessfullmektigView(
                        virksomhet = BehandlingDetaljerView.VirksomhetView(
                            virksomhetsnummer = organisasjon.organisasjonsnummer,
                            navn = organisasjon.navn.sammensattNavn()
                        ),
                        person = null,
                    )
                }
            }
        }
}