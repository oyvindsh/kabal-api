package no.nav.klage.oppgave.service


import io.micrometer.core.instrument.MeterRegistry
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.kodeverk.Type
import no.nav.klage.kodeverk.Ytelse
import no.nav.klage.kodeverk.hjemmel.Hjemmel
import no.nav.klage.kodeverk.hjemmel.ytelseTilHjemler
import no.nav.klage.oppgave.api.view.*
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.norg2.Norg2Client
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.config.incrementMottattKlageAnke
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.events.MottakLagretEvent
import no.nav.klage.oppgave.domain.klage.*
import no.nav.klage.oppgave.domain.kodeverk.LovligeTyper
import no.nav.klage.oppgave.eventlisteners.CreateBehandlingFromMottakEventListener
import no.nav.klage.oppgave.exceptions.*
import no.nav.klage.oppgave.gateway.AzureGateway
import no.nav.klage.oppgave.repositories.AnkebehandlingRepository
import no.nav.klage.oppgave.repositories.BehandlingRepository
import no.nav.klage.oppgave.repositories.KlagebehandlingRepository
import no.nav.klage.oppgave.repositories.MottakRepository
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import no.nav.klage.oppgave.util.isValidFnrOrDnr
import no.nav.klage.oppgave.util.isValidOrgnr
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class MottakService(
    environment: Environment,
    private val mottakRepository: MottakRepository,
    private val klagebehandlingRepository: KlagebehandlingRepository,
    private val ankebehandlingRepository: AnkebehandlingRepository,
    private val behandlingRepository: BehandlingRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val dokumentService: DokumentService,
    private val norg2Client: Norg2Client,
    private val azureGateway: AzureGateway,
    private val meterRegistry: MeterRegistry,
    private val createBehandlingFromMottakEventListener: CreateBehandlingFromMottakEventListener,
    private val pdlFacade: PdlFacade,
    private val eregClient: EregClient,
) {

    private val lovligeTyperIMottakV2 = LovligeTyper.lovligeTyper(environment)


    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    @Transactional
    fun createMottakForKlageV2(oversendtKlage: OversendtKlageV2) {
        secureLogger.debug("Prøver å lagre oversendtKlageV2: {}", oversendtKlage)
        oversendtKlage.validate()

        val mottak = mottakRepository.save(oversendtKlage.toMottak())

        secureLogger.debug("Har lagret følgende mottak basert på en oversendtKlage: {}", mottak)
        logger.debug("Har lagret mottak {}, publiserer nå event", mottak.id)

        publishEventAndUpdateMetrics(
            mottak = mottak,
            kilde = oversendtKlage.kilde.name,
            ytelse = oversendtKlage.ytelse.navn,
            type = oversendtKlage.type.navn,
        )
    }

    @Transactional
    fun createMottakForKlageAnkeV3(oversendtKlageAnke: OversendtKlageAnkeV3) {
        secureLogger.debug("Prøver å lagre oversendtKlageAnkeV3: {}", oversendtKlageAnke)

        val mottak = validateAndSaveMottak(oversendtKlageAnke)

        secureLogger.debug("Har lagret følgende mottak basert på en oversendtKlageAnke: {}", mottak)
        logger.debug("Har lagret mottak {}, publiserer nå event", mottak.id)

        publishEventAndUpdateMetrics(
            mottak = mottak,
            kilde = oversendtKlageAnke.kilde.name,
            ytelse = oversendtKlageAnke.ytelse.navn,
            type = oversendtKlageAnke.type.navn,
        )
    }

    @Transactional
    fun createMottakForKlageAnkeV3ForE2ETests(oversendtKlageAnke: OversendtKlageAnkeV3): Behandling {
        secureLogger.debug("Prøver å lagre oversendtKlageAnkeV3 for E2E-tests: {}", oversendtKlageAnke)

        val mottak = validateAndSaveMottak(oversendtKlageAnke)

        secureLogger.debug("Har lagret følgende mottak basert på en oversendtKlageAnkeV3: {}", mottak)
        logger.debug("Har lagret mottak {}, publiserer nå event", mottak.id)

        meterRegistry.incrementMottattKlageAnke(
            oversendtKlageAnke.kilde.name,
            oversendtKlageAnke.ytelse.navn,
            oversendtKlageAnke.type.navn
        )

        return createBehandlingFromMottakEventListener.createBehandling(MottakLagretEvent(mottak))
    }

    private fun publishEventAndUpdateMetrics(
        mottak: Mottak,
        kilde: String,
        ytelse: String,
        type: String
    ) {
        applicationEventPublisher.publishEvent(MottakLagretEvent(mottak))

        meterRegistry.incrementMottattKlageAnke(
            kildesystem = kilde,
            ytelse = ytelse,
            type = type
        )
    }

    private fun validateAndSaveMottak(oversendtKlageAnke: OversendtKlageAnkeV3): Mottak {
        oversendtKlageAnke.validate()

        val mottak =
            when (oversendtKlageAnke.type) {
                Type.KLAGE -> mottakRepository.save(oversendtKlageAnke.toMottak())
                Type.ANKE -> {
                    val previousHandledKlage =
                        klagebehandlingRepository.findByKildeReferanseAndYtelse(
                            oversendtKlageAnke.kildeReferanse,
                            oversendtKlageAnke.ytelse
                        )
                    if (previousHandledKlage != null) {
                        logger.debug("Fant tidligere behandlet klage i Kabal, med id ${previousHandledKlage.id}")
                        if (oversendtKlageAnke.dvhReferanse != previousHandledKlage.dvhReferanse) {
                            val message =
                                "Tidligere behandlet klage har annen dvhReferanse enn innsendt anke."
                            logger.warn(message)
                            throw OversendtKlageNotValidException(message)
                        }
                        mottakRepository.save(oversendtKlageAnke.toMottak(previousHandledKlage.id))
                    } else {
                        mottakRepository.save(oversendtKlageAnke.toMottak())
                    }
                }

                Type.ANKE_I_TRYGDERETTEN -> TODO()
            }
        return mottak
    }

    @Transactional
    fun createAnkeMottakBasertPaaKlagebehandlingId(input: AnkeBasertPaaKlageInput): Behandling {
        val klagebehandlingId = input.klagebehandlingId
        logger.debug("Prøver å lagre anke basert på klagebehandlingId {}", klagebehandlingId)
        val klagebehandling = klagebehandlingRepository.getReferenceById(klagebehandlingId)

        validateAnkeCreationBasedOnKlagebehandling(klagebehandling, klagebehandlingId)

        val mottak = mottakRepository.save(klagebehandling.toAnkeMottak(input.innsendtAnkeJournalpostId))

        logger.debug("Har lagret mottak {}, basert på innsendt klagebehandlingId: {}", mottak.id, klagebehandlingId)

        return createBehandlingFromMottakEventListener.createBehandling(MottakLagretEvent(mottak))
    }

    @Transactional
    fun createAnkeMottakFromKabinInput(input: CreateAnkeBasedOnKabinInput) {
        val klagebehandlingId = input.klagebehandlingId
        logger.debug("Prøver å lagre anke basert på Kabin-input med klagebehandlingId {}", klagebehandlingId)
        val klagebehandling = klagebehandlingRepository.getReferenceById(klagebehandlingId)

        validateAnkeCreationBasedOnKlagebehandling(klagebehandling, klagebehandlingId)

        val mottak = mottakRepository.save(klagebehandling.toAnkeMottak(input))

        publishEventAndUpdateMetrics(
            mottak = mottak,
            kilde = mottak.sakFagsystem.navn,
            ytelse = mottak.ytelse.navn,
            type = mottak.type.navn,
        )

        logger.debug(
            "Har lagret mottak {}, basert på innsendt klagebehandlingId: {} fra Kabin",
            mottak.id,
            klagebehandlingId
        )
    }

    fun validateAnkeCreationBasedOnKlagebehandling(
        klagebehandling: Klagebehandling,
        klagebehandlingId: UUID
    ) {
        if (!klagebehandling.isAvsluttet()) {
            throw PreviousBehandlingNotFinalizedException("Klagebehandling med id $klagebehandlingId er ikke fullført")
        }


        val existingAnke = ankebehandlingRepository.findByKlagebehandlingId(klagebehandlingId)

        if (existingAnke != null) {
            val message =
                "Anke har allerede blir opprettet på klagebehandling med id $klagebehandlingId"
            logger.warn(message)
            throw DuplicateOversendelseException(message)
        }
    }

    fun OversendtKlageV2.validate() {
        validateDuplicate(kilde, kildeReferanse, type)
        validateYtelseAndHjemler(ytelse, hjemler)
        tilknyttedeJournalposter.forEach { validateJournalpost(it.journalpostId) }
        validatePartId(klager.id)
        sakenGjelder?.run { validatePartId(sakenGjelder.id) }
        validateType(type)
        validateEnhet(avsenderEnhet)
        validateKildeReferanse(kildeReferanse)
        validateDateNotInFuture(mottattFoersteinstans, ::mottattFoersteinstans.name)
        validateDateNotInFuture(innsendtTilNav, ::innsendtTilNav.name)
        validateOptionalDateTimeNotInFuture(oversendtKaDato, ::oversendtKaDato.name)
        validateSaksbehandler(avsenderSaksbehandlerIdent, avsenderEnhet)
    }

    fun OversendtKlageAnkeV3.validate() {
        validateYtelseAndHjemler(ytelse, hjemler)
        validateDuplicate(kilde, kildeReferanse, type)
        tilknyttedeJournalposter.forEach { validateJournalpost(it.journalpostId) }
        validatePartId(klager.id)
        sakenGjelder?.run { validatePartId(sakenGjelder.id) }
        validateDateNotInFuture(brukersHenvendelseMottattNavDato, ::brukersHenvendelseMottattNavDato.name)
        validateDateNotInFuture(innsendtTilNav, ::innsendtTilNav.name)
        validateDateNotInFuture(sakMottattKaDato, ::sakMottattKaDato.name)
        validateKildeReferanse(kildeReferanse)
        validateEnhet(forrigeBehandlendeEnhet)
    }

    private fun validateDuplicate(sakFagsystem: KildeFagsystem, kildeReferanse: String, type: Type) {
        if (mottakRepository.existsBySakFagsystemAndKildeReferanseAndType(
                sakFagsystem.mapFagsystem(),
                kildeReferanse,
                type
            )
        ) {
            val message =
                "Kunne ikke lagre oversendelse grunnet duplikat: kildesystem ${sakFagsystem.name} og kildereferanse: $kildeReferanse"
            logger.warn(message)
            throw DuplicateOversendelseException(message)
        }
    }

    private fun validateOptionalDateTimeNotInFuture(inputDateTime: LocalDateTime?, parameterName: String) {
        if (inputDateTime != null && LocalDateTime.now().isBefore(inputDateTime))
            throw OversendtKlageNotValidException("$parameterName kan ikke være i fremtiden, innsendt dato var $inputDateTime.")
    }

    private fun validateDateNotInFuture(inputDate: LocalDate?, parameterName: String) {
        if (inputDate != null && LocalDate.now().isBefore(inputDate))
            throw OversendtKlageNotValidException("$parameterName kan ikke være i fremtiden, innsendt dato var $inputDate.")
    }

    private fun validateKildeReferanse(kildeReferanse: String) {
        if (kildeReferanse.isEmpty())
            throw OversendtKlageNotValidException("Kildereferanse kan ikke være en tom streng.")
    }

    private fun validateYtelseAndHjemler(ytelse: Ytelse, hjemler: Collection<Hjemmel>?) {
        if (ytelse in ytelseTilHjemler.keys) {
            if (!hjemler.isNullOrEmpty()) {
                hjemler.forEach {
                    if (!ytelseTilHjemler[ytelse]!!.contains(it)) {
                        throw OversendtKlageNotValidException("Behandling med ytelse ${ytelse.navn} kan ikke registreres med hjemmel $it. Ta kontakt med team klage dersom du mener hjemmelen skal være mulig å bruke for denne ytelsen.")
                    }
                }
            }
        } else {
            throw OversendtKlageNotValidException("Behandling med ytelse ${ytelse.navn} kan ikke registreres. Ta kontakt med team klage dersom du vil ta i bruk ytelsen.")
        }
    }

    private fun validateType(type: Type) {
        if (!lovligeTyperIMottakV2.contains(type)) {
            throw OversendtKlageNotValidException("Kabal kan ikke motta klager med type $type ennå")
        }
    }

    private fun validateSaksbehandler(saksbehandlerident: String, enhetNr: String) {
        if (azureGateway.getPersonligDataOmSaksbehandlerMedIdent(saksbehandlerident).enhet.enhetId != enhetNr) {
            //throw OversendtKlageNotValidException("$saksbehandlerident er ikke saksbehandler i enhet $enhet")
            logger.warn("$saksbehandlerident er ikke saksbehandler i enhet $enhetNr")
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

    private fun validatePartId(partId: OversendtPartId) {
        when (partId.type) {
            OversendtPartIdType.VIRKSOMHET -> {
                if (!isValidOrgnr(partId.verdi)) {
                    throw OversendtKlageNotValidException("Ugyldig organisasjonsnummer")
                }
                if (!eregClient.organisasjonExists(partId.verdi)) {
                    throw OversendtKlageNotValidException("Organisasjonen fins ikke i Ereg")
                }
            }

            OversendtPartIdType.PERSON -> {
                if (!isValidFnrOrDnr(partId.verdi)) {
                    throw OversendtKlageNotValidException("Ugyldig fødselsnummer")
                }

                if (!pdlFacade.personExists(partId.verdi)) {
                    throw OversendtKlageNotValidException("Personen fins ikke i PDL")
                }
            }
        }
    }

    private fun Klagebehandling.toAnkeMottak(innsendtAnkeJournalpostId: String?): Mottak {
        val innsendtDokument =
            if (innsendtAnkeJournalpostId != null) {
                mutableSetOf(
                    MottakDokument(
                        type = MottakDokumentType.BRUKERS_ANKE,
                        journalpostId = innsendtAnkeJournalpostId
                    )
                )
            } else mutableSetOf()

        return Mottak(
            type = Type.ANKE,
            klager = klager,
            sakenGjelder = sakenGjelder,
            sakFagsystem = sakFagsystem,
            sakFagsakId = sakFagsakId,
            kildeReferanse = kildeReferanse,
            dvhReferanse = dvhReferanse,
            //Dette er søkehjemler
            hjemler = mottakRepository.getReferenceById(id).hjemler,
            forrigeSaksbehandlerident = tildeling!!.saksbehandlerident,
            forrigeBehandlendeEnhet = tildeling!!.enhet!!,
            mottakDokument = innsendtDokument,
            innsendtDato = LocalDate.now(),
            brukersHenvendelseMottattNavDato = LocalDate.now(),
            sakMottattKaDato = LocalDateTime.now(),
            fristFraFoersteinstans = null,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            ytelse = ytelse,
            kommentar = null,
            forrigeBehandlingId = id,
            innsynUrl = null,
            sentFrom = Mottak.Sender.BRUKER,
        )
    }

    private fun Klagebehandling.toAnkeMottak(input: CreateAnkeBasedOnKabinInput): Mottak {
        val prosessfullmektig = if (input.prosessfullmektig != null) {
            Prosessfullmektig(
                partId = PartId(
                    type = PartIdType.of(input.prosessfullmektig.id.type.name),
                    value = input.prosessfullmektig.id.verdi
                ),
                skalPartenMottaKopi = true
            )
        } else {
            null
        }

        val klager = if (input.klager != null) {
            Klager(
                partId = PartId(
                    type = PartIdType.of(input.klager.id.type.name),
                    value = input.klager.id.verdi
                ),
                prosessfullmektig = prosessfullmektig
            )
        } else {
            Klager(
                partId = PartId(
                    type = PartIdType.of(sakenGjelder.partId.type.name),
                    value = sakenGjelder.partId.value
                ),
                prosessfullmektig = prosessfullmektig
            )
        }

        val innsendtDokument =
            mutableSetOf(
                MottakDokument(
                    type = MottakDokumentType.BRUKERS_ANKE,
                    journalpostId = input.ankeDocumentJournalpostId
                )
            )

        return Mottak(
            type = Type.ANKE,
            klager = klager,
            sakenGjelder = sakenGjelder,
            sakFagsystem = sakFagsystem,
            sakFagsakId = sakFagsakId,
            kildeReferanse = kildeReferanse,
            dvhReferanse = dvhReferanse,
            //Dette er søkehjemler
            hjemler = mottakRepository.getReferenceById(id).hjemler,
            forrigeSaksbehandlerident = tildeling!!.saksbehandlerident,
            forrigeBehandlendeEnhet = tildeling!!.enhet!!,
            mottakDokument = innsendtDokument,
            innsendtDato = input.mottattNav,
            brukersHenvendelseMottattNavDato = input.mottattNav,
            sakMottattKaDato = input.mottattNav.atStartOfDay(),
            fristFraFoersteinstans = null,
            created = LocalDateTime.now(),
            modified = LocalDateTime.now(),
            ytelse = ytelse,
            kommentar = null,
            forrigeBehandlingId = id,
            innsynUrl = null,
            sentFrom = Mottak.Sender.KABIN,
        )
    }
}
