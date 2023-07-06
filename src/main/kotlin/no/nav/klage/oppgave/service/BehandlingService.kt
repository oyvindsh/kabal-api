package no.nav.klage.oppgave.service

import no.nav.klage.dokument.repositories.DokumentUnderArbeidRepository
import no.nav.klage.kodeverk.*
import no.nav.klage.kodeverk.MedunderskriverFlyt.OVERSENDT_TIL_MEDUNDERSKRIVER
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.Registreringshjemmel
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.arbeidoginntekt.ArbeidOgInntektClient
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Medunderskrivere
import no.nav.klage.oppgave.clients.kabalinnstillinger.model.Saksbehandlere
import no.nav.klage.oppgave.clients.kaka.KakaApiGateway
import no.nav.klage.oppgave.clients.klagefssproxy.KlageFssProxyClient
import no.nav.klage.oppgave.clients.klagefssproxy.domain.HandledInKabalInput
import no.nav.klage.oppgave.clients.klagefssproxy.domain.SakAssignedInput
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandlingSetters.setKjennelseMottatt
import no.nav.klage.oppgave.domain.klage.AnkeITrygderettenbehandlingSetters.setSendtTilTrygderetten
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.addSaksdokument
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.removeSaksdokument
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setAvsluttetAvSaksbehandler
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setFeilregistrering
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setFrist
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setFullmektig
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setInnsendingshjemler
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setKlager
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setMedunderskriverFlyt
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setMedunderskriverIdentAndMedunderskriverFlyt
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setMottattKlageinstans
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setROLIdent
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setROLState
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setRegistreringshjemler
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setSattPaaVent
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setTildeling
import no.nav.klage.oppgave.domain.klage.BehandlingSetters.setUtfallInVedtak
import no.nav.klage.oppgave.domain.klage.KlagebehandlingSetters.setMottattVedtaksinstans
import no.nav.klage.oppgave.exceptions.*
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.repositories.SaksbehandlerRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.isValidFnrOrDnr
import no.nav.klage.oppgave.util.isValidOrgnr
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
@Transactional
class BehandlingService(
    private val behandlingRepository: BehandlingRepository,
    private val tilgangService: TilgangService,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val kakaApiGateway: KakaApiGateway,
    private val dokumentService: DokumentService,
    private val dokumentUnderArbeidRepository: DokumentUnderArbeidRepository,
    private val kabalInnstillingerService: KabalInnstillingerService,
    private val innloggetSaksbehandlerService: InnloggetSaksbehandlerService,
    private val arbeidOgInntektClient: ArbeidOgInntektClient,
    private val fssProxyClient: KlageFssProxyClient,
    private val saksbehandlerRepository: SaksbehandlerRepository,
    private val eregClient: EregClient,
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }


    fun ferdigstillBehandling(
        behandlingId: UUID,
        innloggetIdent: String
    ): Behandling {
        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId
        )

        if (behandling.avsluttetAvSaksbehandler != null) throw BehandlingFinalizedException("Behandlingen er avsluttet")

        //Forretningsmessige krav før vedtak kan ferdigstilles
        validateBehandlingBeforeFinalize(behandling)

        //Her settes en markør som så brukes async i kallet klagebehandlingRepository.findByAvsluttetIsNullAndAvsluttetAvSaksbehandlerIsNotNull
        return markerBehandlingSomAvsluttetAvSaksbehandler(behandling, innloggetIdent)
    }

    private fun markerBehandlingSomAvsluttetAvSaksbehandler(
        behandling: Behandling,
        innloggetIdent: String
    ): Behandling {
        val event = behandling.setAvsluttetAvSaksbehandler(innloggetIdent)
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun validateBehandlingBeforeFinalize(behandling: Behandling) {
        val dokumentValidationErrors = mutableListOf<InvalidProperty>()
        val behandlingValidationErrors = mutableListOf<InvalidProperty>()
        val sectionList = mutableListOf<ValidationSection>()

        val unfinishedDocuments =
            dokumentUnderArbeidRepository.findByBehandlingIdAndMarkertFerdigIsNull(behandling.id)

        if (unfinishedDocuments.isNotEmpty()) {
            dokumentValidationErrors.add(
                InvalidProperty(
                    field = "underArbeid",
                    reason = "Ferdigstill eller slett alle dokumenter under arbeid."
                )
            )
        }

        if (dokumentValidationErrors.isNotEmpty()) {
            sectionList.add(
                ValidationSection(
                    section = "dokumenter",
                    properties = dokumentValidationErrors
                )
            )
        }

        if (behandling.utfall == null) {
            behandlingValidationErrors.add(
                InvalidProperty(
                    field = "utfall",
                    reason = "Sett et utfall på vedtaket."
                )
            )
        }

        //TODO: Create test for invalid utfall when such are added
        if (behandling.utfall != null && behandling.utfall !in typeTilUtfall[behandling.type]!!) {
            behandlingValidationErrors.add(
                InvalidProperty(
                    field = "utfall",
                    reason = "Dette utfallet er ikke gyldig for denne behandlingstypen."
                )
            )
        }

        if (behandling.utfall !in noRegistringshjemmelNeeded) {
            if (behandling.hjemler.isEmpty()) {
                behandlingValidationErrors.add(
                    InvalidProperty(
                        field = "hjemmel",
                        reason = "Sett en eller flere hjemler på vedtaket."
                    )
                )
            }
        }

        if (behandling !is AnkeITrygderettenbehandling && behandling.utfall !in noKvalitetsvurderingNeeded) {
            val kvalitetsvurderingValidationErrors = kakaApiGateway.getValidationErrors(behandling)

            if (kvalitetsvurderingValidationErrors.isNotEmpty()) {
                sectionList.add(
                    ValidationSection(
                        section = "kvalitetsvurdering",
                        properties = kvalitetsvurderingValidationErrors
                    )
                )
            }
        }

        if (LocalDateTime.now().isBefore(behandling.mottattKlageinstans)) {
            behandlingValidationErrors.add(
                InvalidProperty(
                    field = "mottattKlageinstans",
                    reason = "Denne datoen kan ikke være i fremtiden."
                )
            )
        }

        if (behandling is Klagebehandling &&
            LocalDate.now().isBefore(behandling.mottattVedtaksinstans)
        ) {
            behandlingValidationErrors.add(
                InvalidProperty(
                    field = "mottattVedtaksinstans",
                    reason = "Denne datoen kan ikke være i fremtiden."
                )
            )
        }

        if (behandling is AnkeITrygderettenbehandling) {
            if (behandling.kjennelseMottatt == null) {
                behandlingValidationErrors.add(
                    InvalidProperty(
                        field = "kjennelseMottatt",
                        reason = "Denne datoen må være satt."
                    )
                )
            }

            if (behandling.kjennelseMottatt != null
                && behandling.sendtTilTrygderetten.isAfter(behandling.kjennelseMottatt)
            ) {
                behandlingValidationErrors.add(
                    InvalidProperty(
                        field = "sendtTilTrygderetten",
                        reason = "Sendt til Trygderetten må være før Kjennelse mottatt."
                    )
                )
            }
        }

        if (behandling.klager.prosessfullmektig?.isPerson() == false &&
            !eregClient.hentOrganisasjon(behandling.klager.prosessfullmektig!!.partId.value).isActive()
        ) {
            behandlingValidationErrors.add(
                InvalidProperty(
                    field = "fullmektig",
                    reason = "Fullmektig/organisasjon har opphørt."
                )
            )
        }

        if (behandlingValidationErrors.isNotEmpty()) {
            sectionList.add(
                ValidationSection(
                    section = "behandling",
                    properties = behandlingValidationErrors
                )
            )
        }

        if (sectionList.isNotEmpty()) {
            throw SectionedValidationErrorWithDetailsException(
                title = "Validation error",
                sections = sectionList
            )
        }
    }

    fun setSaksbehandler(
        behandlingId: UUID,
        tildeltSaksbehandlerIdent: String?,
        enhetId: String?,
        utfoerendeSaksbehandlerIdent: String,
    ): Behandling {
        val behandling = getBehandlingForUpdate(behandlingId = behandlingId, ignoreCheckSkrivetilgang = true)
        if (tildeltSaksbehandlerIdent != null) {
            //Denne sjekken gjøres kun når det er en tildeling:

            checkYtelseAccess(tildeltSaksbehandlerIdent = tildeltSaksbehandlerIdent, behandling = behandling)

            //if fagsystem is Infotrygd also do this.
            if (behandling.fagsystem == Fagsystem.IT01) {
                logger.debug("Tildeling av behandling skal registreres i Infotrygd.")
                fssProxyClient.setToAssigned(
                    sakId = behandling.kildeReferanse,
                    input = SakAssignedInput(
                        saksbehandlerIdent = tildeltSaksbehandlerIdent,
                        enhetsnummer = enhetId,
                    )
                )
                logger.debug("Tildeling av behandling ble registrert i Infotrygd.")
            }
        } else {
            if (behandling.medunderskriverFlyt == OVERSENDT_TIL_MEDUNDERSKRIVER) {
                throw IllegalOperation("Kan ikke fradele behandling sendt til medunderskriver.")
            }

            //if fagsystem is Infotrygd also do this.
            if (behandling.fagsystem == Fagsystem.IT01 && behandling.type != Type.ANKE_I_TRYGDERETTEN) {
                logger.debug("Fradeling av behandling skal registreres i Infotrygd.")
                fssProxyClient.setToHandledInKabal(
                    sakId = behandling.kildeReferanse,
                    input = HandledInKabalInput(
                        fristAsString = behandling.frist!!.format(DateTimeFormatter.BASIC_ISO_DATE),
                    )
                )
                logger.debug("Fradeling av behandling ble registrert i Infotrygd.")
            }

            //Fjern på vent-status
            setSattPaaVent(
                behandlingId = behandlingId,
                utfoerendeSaksbehandlerIdent = utfoerendeSaksbehandlerIdent,
                sattPaaVent = null,
                systemUserContext = saksbehandlerRepository.hasKabalOppgavestyringAlleEnheterRole(
                    utfoerendeSaksbehandlerIdent
                ),
            )
        }

        val event =
            behandling.setTildeling(
                nyVerdiSaksbehandlerident = tildeltSaksbehandlerIdent,
                nyVerdiEnhet = enhetId,
                saksbehandlerident = utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun setSattPaaVent(
        behandlingId: UUID,
        utfoerendeSaksbehandlerIdent: String,
        sattPaaVent: SattPaaVent?,
        systemUserContext: Boolean = false
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId,
            systemUserContext = systemUserContext
        )
        val event =
            behandling.setSattPaaVent(
                sattPaaVent,
                utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling.modified
    }

    fun setFrist(
        behandlingId: UUID,
        frist: LocalDate,
        utfoerendeSaksbehandlerIdent: String,
    ): LocalDateTime {
        if (!innloggetSaksbehandlerService.isKabalOppgavestyringAlleEnheter()) {
            throw MissingTilgangException("$utfoerendeSaksbehandlerIdent does not have the right to modify frist")
        }

        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId,
            ignoreCheckSkrivetilgang = true
        )
        val event = behandling.setFrist(frist, utfoerendeSaksbehandlerIdent)

        applicationEventPublisher.publishEvent(event)
        return behandling.modified
    }

    fun setMottattKlageinstans(
        behandlingId: UUID,
        date: LocalDateTime,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = if (innloggetSaksbehandlerService.isKabalOppgavestyringAlleEnheter()) {
            getBehandlingForUpdate(
                behandlingId = behandlingId,
                ignoreCheckSkrivetilgang = true,
            )
        } else {
            getBehandlingForUpdate(behandlingId)
        }

        val event = behandling.setMottattKlageinstans(date, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return behandling.modified
    }

    fun setMottattVedtaksinstans(
        behandlingId: UUID,
        date: LocalDate,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )

        if (behandling is Klagebehandling) {
            val event =
                behandling.setMottattVedtaksinstans(date, utfoerendeSaksbehandlerIdent)
            applicationEventPublisher.publishEvent(event)
            return behandling.modified
        } else throw IllegalOperation("Dette feltet kan bare settes i klagesaker")
    }

    fun setSendtTilTrygderetten(
        behandlingId: UUID,
        date: LocalDateTime,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )

        if (behandling is AnkeITrygderettenbehandling) {
            val event =
                behandling.setSendtTilTrygderetten(date, utfoerendeSaksbehandlerIdent)
            applicationEventPublisher.publishEvent(event)
            return behandling.modified
        } else throw IllegalOperation("Dette feltet kan bare settes i ankesaker i Trygderetten")
    }

    fun setKjennelseMottatt(
        behandlingId: UUID,
        date: LocalDateTime,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )

        if (behandling is AnkeITrygderettenbehandling) {
            val event =
                behandling.setKjennelseMottatt(date, utfoerendeSaksbehandlerIdent)
            applicationEventPublisher.publishEvent(event)
            return behandling.modified
        } else throw IllegalOperation("Dette feltet kan bare settes i ankesaker i Trygderetten")
    }

    fun setInnsendingshjemler(
        behandlingId: UUID,
        hjemler: List<String>,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )

        val event =
            behandling.setInnsendingshjemler(
                hjemler.map { Hjemmel.of(it) }.toSet(),
                utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling.modified
    }

    fun setFullmektig(
        behandlingId: UUID,
        identifikator: String?,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )

        val partId: PartId? = if (identifikator == null) {
            null
        } else {
            getPartIdFromIdentifikator(identifikator)
        }

        val event =
            behandling.setFullmektig(
                partId,
                utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling.modified
    }

    fun setKlager(
        behandlingId: UUID,
        identifikator: String,
        utfoerendeSaksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )

        val event =
            behandling.setKlager(
                nyVerdi = getPartIdFromIdentifikator(identifikator),
                saksbehandlerident = utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling.modified
    }

    fun getPartIdFromIdentifikator(identifikator: String): PartId =
        when (identifikator.length) {
            11 -> {
                if (isValidFnrOrDnr(identifikator)) {
                    PartId(
                        type = PartIdType.PERSON,
                        value = identifikator
                    )
                } else {
                    throw ValidationException("identifier is not a valid fødselsnummer")
                }
            }

            9 -> {
                if (isValidOrgnr(identifikator)) {
                    PartId(
                        type = PartIdType.VIRKSOMHET,
                        value = identifikator
                    )
                } else {
                    throw ValidationException("identifier is not a valid organisasjonsnummer")
                }
            }

            else -> {
                throw ValidationException("identifier is not a valid. Unknown type.")
            }
        }

    fun setMedunderskriverIdentAndMedunderskriverFlyt(
        behandlingId: UUID,
        medunderskriverIdent: String?,
        utfoerendeSaksbehandlerIdent: String,
        medunderskriverFlyt: MedunderskriverFlyt = MedunderskriverFlyt.IKKE_SENDT
    ): Behandling {
        val behandling =
            if (saksbehandlerRepository.hasKabalOppgavestyringAlleEnheterRole(utfoerendeSaksbehandlerIdent)) {
                getBehandlingForUpdate(behandlingId = behandlingId, ignoreCheckSkrivetilgang = true)
            } else {
                getBehandlingForUpdate(behandlingId = behandlingId)
            }

        val event =
            behandling.setMedunderskriverIdentAndMedunderskriverFlyt(
                medunderskriverIdent,
                medunderskriverFlyt,
                utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun switchMedunderskriverFlyt(
        behandlingId: UUID,
        utfoerendeSaksbehandlerIdent: String
    ): Behandling {
        val behandling = getBehandling(behandlingId)

        if (behandling.medunderskriver?.saksbehandlerident == null) {
            throw BehandlingManglerMedunderskriverException("Behandlingen har ikke registrert noen medunderskriver")
        }

        if (behandling.medunderskriver?.saksbehandlerident == utfoerendeSaksbehandlerIdent) {
            verifyMedunderskriverStatusAndBehandlingNotFinalized(behandling)
            if (behandling.medunderskriverFlyt != MedunderskriverFlyt.RETURNERT_TIL_SAKSBEHANDLER) {
                val event = behandling.setMedunderskriverFlyt(
                    MedunderskriverFlyt.RETURNERT_TIL_SAKSBEHANDLER,
                    utfoerendeSaksbehandlerIdent
                )
                applicationEventPublisher.publishEvent(event)
            }
        } else {
            checkSkrivetilgang(behandling)
            if (behandling.medunderskriverFlyt != OVERSENDT_TIL_MEDUNDERSKRIVER) {
                val event = behandling.setMedunderskriverFlyt(
                    OVERSENDT_TIL_MEDUNDERSKRIVER,
                    utfoerendeSaksbehandlerIdent
                )
                applicationEventPublisher.publishEvent(event)
            }
        }

        return behandling
    }

    fun fetchDokumentlisteForBehandling(
        behandlingId: UUID,
        temaer: List<Tema>,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        val behandling = getBehandling(behandlingId)
        return dokumentService.fetchDokumentlisteForBehandling(behandling, temaer, pageSize, previousPageRef)
    }

    fun fetchJournalposterConnectedToBehandling(behandlingId: UUID): DokumenterResponse {
        val behandling = getBehandling(behandlingId)
        return dokumentService.fetchJournalposterConnectedToBehandling(behandling)
    }

    fun connectDokumentToBehandling(
        behandlingId: UUID,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String,
        systemUserContext: Boolean = false
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId,
            ignoreCheckSkrivetilgang = systemUserContext,
            systemUserContext = systemUserContext,
        )
        if (systemUserContext) {
            dokumentService.validateJournalpostExistsAsSystembruker(journalpostId)
        } else {
            dokumentService.validateJournalpostExists(journalpostId)
        }

        addDokument(
            behandling,
            journalpostId,
            dokumentInfoId,
            saksbehandlerIdent
        )
        return behandling.modified
    }

    fun disconnectDokumentFromBehandling(
        behandlingId: UUID,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ): LocalDateTime {
        val behandling = getBehandlingForUpdate(behandlingId)
        val saksdokument =
            behandling.saksdokumenter.find { it.journalpostId == journalpostId && it.dokumentInfoId == dokumentInfoId }

        if (saksdokument == null) {
            logger.warn("no saksdokument found based on id $journalpostId/$dokumentInfoId")
        } else {
            removeDokument(
                behandling,
                saksdokument,
                saksbehandlerIdent
            )
        }
        return behandling.modified
    }

    fun getBehandlingForUpdate(
        behandlingId: UUID,
        ignoreCheckSkrivetilgang: Boolean = false,
        systemUserContext: Boolean = false
    ): Behandling =
        behandlingRepository.findById(behandlingId).get()
            .also {
                if (!systemUserContext && it.feilregistrering != null) {
                    throw BehandlingAvsluttetException("Behandlingen er feilregistrert")
                }
            }
            .also { if (!systemUserContext) checkLeseTilgang(it) }
            .also { if (!systemUserContext && !ignoreCheckSkrivetilgang) checkSkrivetilgang(it) }

    fun checkLeseTilgang(behandling: Behandling) {
        if (behandling.sakenGjelder.erPerson()) {
            checkLeseTilgang(behandling.sakenGjelder.partId.value)
        }
    }

    fun checkLeseTilgang(partIdValue: String) {
        tilgangService.verifyInnloggetSaksbehandlersTilgangTil(partIdValue)
    }

    private fun checkSkrivetilgang(behandling: Behandling) {
        tilgangService.verifyInnloggetSaksbehandlersSkrivetilgang(behandling)
    }

    private fun checkSkrivetilgangForSystembruker(behandling: Behandling) {
        tilgangService.verifySystembrukersSkrivetilgang(behandling)
    }

    @Transactional(readOnly = true)
    fun getBehandlingForReadWithoutCheckForAccess(behandlingId: UUID): Behandling =
        behandlingRepository.findById(behandlingId)
            .orElseThrow { BehandlingNotFoundException("Behandling med id $behandlingId ikke funnet") }

    fun getBehandlingForUpdateBySystembruker(
        behandlingId: UUID,
    ): Behandling =
        behandlingRepository.getReferenceById(behandlingId)
            .also { checkSkrivetilgangForSystembruker(it) }

    private fun checkYtelseAccess(
        tildeltSaksbehandlerIdent: String,
        behandling: Behandling
    ) {
        tilgangService.verifySaksbehandlersAccessToYtelse(
            saksbehandlerIdent = tildeltSaksbehandlerIdent,
            ytelse = behandling.ytelse,
        )
    }

    //TODO: Se om ansvar for sjekk av medunderskriver og finalize kan deles opp.
    private fun verifyMedunderskriverStatusAndBehandlingNotFinalized(behandling: Behandling) {
        tilgangService.verifyInnloggetSaksbehandlerErMedunderskriverAndNotFinalized(behandling)
    }

    private fun addDokument(
        behandling: Behandling,
        journalpostId: String,
        dokumentInfoId: String,
        saksbehandlerIdent: String
    ) {
        try {
            val foundSaksdokument =
                behandling.saksdokumenter.find { it.journalpostId == journalpostId && it.dokumentInfoId == dokumentInfoId }
            if (foundSaksdokument != null) {
                logger.debug("Dokument (journalpost: $journalpostId dokumentInfoId: $dokumentInfoId) is already connected to behandling ${behandling.id}, doing nothing")
            } else {
                val saksdokument = Saksdokument(
                    journalpostId = journalpostId,
                    dokumentInfoId = dokumentInfoId
                )
                val event = behandling.addSaksdokument(
                    saksdokument,
                    saksbehandlerIdent
                )
                event?.let { applicationEventPublisher.publishEvent(it) }
            }
        } catch (e: Exception) {
            logger.error("Error connecting journalpost $journalpostId to behandling ${behandling.id}", e)
            throw e
        }
    }

    private fun removeDokument(
        behandling: Behandling,
        saksdokument: Saksdokument,
        saksbehandlerIdent: String
    ): Behandling {
        try {
            val event =
                behandling.removeSaksdokument(
                    saksdokument,
                    saksbehandlerIdent
                )
            event.let { applicationEventPublisher.publishEvent(it) }

            return behandling
        } catch (e: Exception) {
            logger.error("Error disconnecting document ${saksdokument.id} to behandling ${behandling.id}", e)
            throw e
        }
    }

    @Transactional(readOnly = true)
    fun getBehandlingForSmartEditor(behandlingId: UUID, utfoerendeSaksbehandlerIdent: String): Behandling {
        val behandling = behandlingRepository.findById(behandlingId).get()
        if (behandling.medunderskriver?.saksbehandlerident == utfoerendeSaksbehandlerIdent) {
            verifyMedunderskriverStatusAndBehandlingNotFinalized(behandling)
        } else {
            checkSkrivetilgang(behandling)
        }
        return behandling
    }

    @Transactional(readOnly = true)
    fun verifyWriteAccessForSmartEditorDocument(behandlingId: UUID, utfoerendeSaksbehandlerIdent: String) {
        val behandling = behandlingRepository.findById(behandlingId).get()
        if (!(medunderskriverHasTheSmartEditorAccess(behandling, utfoerendeSaksbehandlerIdent) ||
                    saksbehandlerHasTheSmartEditorAccess(behandling, utfoerendeSaksbehandlerIdent))
        ) {
            throw MissingTilgangException("Not assigned to write to document")
        }
    }

    private fun saksbehandlerHasTheSmartEditorAccess(
        behandling: Behandling,
        utfoerendeSaksbehandlerIdent: String
    ) = (behandling.medunderskriverFlyt != OVERSENDT_TIL_MEDUNDERSKRIVER &&
            behandling.tildeling?.saksbehandlerident == utfoerendeSaksbehandlerIdent)

    private fun medunderskriverHasTheSmartEditorAccess(
        behandling: Behandling,
        utfoerendeSaksbehandlerIdent: String
    ) = (behandling.medunderskriverFlyt == OVERSENDT_TIL_MEDUNDERSKRIVER &&
            behandling.medunderskriver?.saksbehandlerident == utfoerendeSaksbehandlerIdent)

    @Transactional(readOnly = true)
    fun getBehandling(behandlingId: UUID): Behandling =
        behandlingRepository.findById(behandlingId)
            .orElseThrow { BehandlingNotFoundException("Behandling med id $behandlingId ikke funnet") }
            .also { checkLeseTilgang(it) }

    @Transactional(readOnly = true)
    fun findBehandlingerForAvslutning(): List<Pair<UUID, Type>> =
        behandlingRepository.findByAvsluttetIsNullAndAvsluttetAvSaksbehandlerIsNotNullAndFeilregistreringIsNull()
            .map { it.id to it.type }

    fun getPotentialSaksbehandlereForBehandling(behandlingId: UUID): Saksbehandlere {
        val behandling = getBehandling(behandlingId)
        return kabalInnstillingerService.getPotentialSaksbehandlere(behandling)
    }

    fun getPotentialMedunderskrivereForBehandling(behandlingId: UUID): Medunderskrivere {
        val behandling = getBehandling(behandlingId)
        return kabalInnstillingerService.getPotentialMedunderskrivere(behandling)
    }

    fun getPotentialROLForBehandling(behandlingId: UUID): Saksbehandlere {
        val behandling = getBehandling(behandlingId)
        return kabalInnstillingerService.getPotentialROL(behandling)
    }

    fun getAllBehandlingerForEnhet(enhet: String): List<Behandling> {
        return behandlingRepository.findByTildelingEnhetAndAvsluttetAvSaksbehandlerIsNullAndFeilregistreringIsNull(
            enhet
        )
    }

    fun getAInntektUrl(behandlingId: UUID): String {
        val behandling = getBehandling(behandlingId = behandlingId)
        return arbeidOgInntektClient.getAInntektUrl(behandling.sakenGjelder.partId.value)
    }

    fun getAARegisterUrl(behandlingId: UUID): String {
        val behandling = getBehandling(behandlingId = behandlingId)
        return arbeidOgInntektClient.getAARegisterUrl(behandling.sakenGjelder.partId.value)
    }

    fun feilregistrer(behandlingId: UUID, reason: String, fagsystem: Fagsystem): Behandling {
        val navIdent = innloggetSaksbehandlerService.getInnloggetIdent()
        val behandlingForCheck = getBehandling(behandlingId)

        val behandling =
            if (saksbehandlerRepository.hasKabalOppgavestyringAlleEnheterRole(navIdent) || behandlingForCheck.tildeling == null) {
                getBehandlingForUpdate(behandlingId = behandlingId, ignoreCheckSkrivetilgang = true)
            } else {
                getBehandlingForUpdate(behandlingId = behandlingId)
            }

        return feilregistrer(behandling = behandling, navIdent = navIdent, reason = reason, fagsystem = fagsystem)
    }

    private fun feilregistrer(
        behandling: Behandling,
        navIdent: String,
        reason: String,
        fagsystem: Fagsystem
    ): Behandling {
        val event = behandling.setFeilregistrering(
            feilregistrering = Feilregistrering(
                navIdent = navIdent,
                registered = LocalDateTime.now(),
                reason = reason,
                fagsystem = fagsystem,
            ),
            saksbehandlerident = navIdent,
        )
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun setUtfall(
        behandlingId: UUID,
        utfall: Utfall?,
        utfoerendeSaksbehandlerIdent: String
    ): Behandling {
        val behandling = getBehandlingForUpdate(
            behandlingId
        )
        val event =
            behandling.setUtfallInVedtak(utfall, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun setRegistreringshjemler(
        behandlingId: UUID,
        registreringshjemler: Set<Registreringshjemmel>,
        utfoerendeSaksbehandlerIdent: String,
        systemUserContext: Boolean = false
    ): Behandling {
        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId,
            systemUserContext = systemUserContext,
        )
        //TODO: Versjonssjekk på input
        val event =
            behandling.setRegistreringshjemler(registreringshjemler, utfoerendeSaksbehandlerIdent)
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun setROLState(
        behandlingId: UUID,
        rolState: ROLState?,
        utfoerendeSaksbehandlerIdent: String,
        systemUserContext: Boolean = false
    ): Behandling {
        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId,
            systemUserContext = systemUserContext,
        )
        val event =
            behandling.setROLState(
                newROLState = rolState,
                saksbehandlerident = utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling
    }

    fun setROLIdent(
        behandlingId: UUID,
        rolIdent: String?,
        utfoerendeSaksbehandlerIdent: String,
        systemUserContext: Boolean = false
    ): Behandling {
        val behandling = getBehandlingForUpdate(
            behandlingId = behandlingId,
            systemUserContext = systemUserContext,
        )
        val event =
            behandling.setROLIdent(
                newROLIdent = rolIdent,
                saksbehandlerident = utfoerendeSaksbehandlerIdent
            )
        applicationEventPublisher.publishEvent(event)
        return behandling
    }
}