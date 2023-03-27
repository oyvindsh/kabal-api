package no.nav.klage.oppgave.service

import no.nav.klage.kodeverk.Tema
import no.nav.klage.oppgave.api.view.DokumentReferanse
import no.nav.klage.oppgave.api.view.DokumenterResponse
import no.nav.klage.oppgave.clients.saf.graphql.*
import no.nav.klage.oppgave.clients.saf.rest.ArkivertDokument
import no.nav.klage.oppgave.clients.saf.rest.SafRestClient
import no.nav.klage.oppgave.domain.Behandling
import no.nav.klage.oppgave.domain.klage.Klagebehandling
import no.nav.klage.oppgave.domain.klage.Saksdokument
import no.nav.klage.oppgave.exceptions.JournalpostNotFoundException
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class DokumentService(
    private val safGraphQlClient: SafGraphQlClient,
    private val safRestClient: SafRestClient
) {
    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
        private val dokumentMapper = DokumentMapper()
    }

    fun fetchDokumentlisteForBehandling(
        behandling: Behandling,
        temaer: List<Tema>,
        pageSize: Int,
        previousPageRef: String?
    ): DokumenterResponse {
        if (behandling.sakenGjelder.erPerson()) {
            val dokumentoversiktBruker: DokumentoversiktBruker =
                safGraphQlClient.getDokumentoversiktBruker(
                    behandling.sakenGjelder.partId.value,
                    mapTema(temaer),
                    pageSize,
                    previousPageRef
                )

            //Attempt to track down elusive bug with repeated documents
            val uniqueJournalposter = dokumentoversiktBruker.journalposter.map { it.journalpostId }.toSet()
            if (uniqueJournalposter.size != dokumentoversiktBruker.journalposter.size) {
                secureLogger.error(
                    "Received list of non unique documents from SAF.\nUnique list: ${
                        uniqueJournalposter.joinToString()
                    }. " + "\nFull list: ${
                        dokumentoversiktBruker.journalposter.joinToString { it.journalpostId }
                    }" + "\nParams: fnr: ${behandling.sakenGjelder.partId.value}, behandlingId: ${behandling.id}, " +
                            "temaer: ${temaer.joinToString()}, pageSize: $pageSize, previousPageRef: $previousPageRef"
                )
            }

            return DokumenterResponse(
                dokumenter = dokumentoversiktBruker.journalposter.map { journalpost ->
                    dokumentMapper.mapJournalpostToDokumentReferanse(journalpost, behandling)
                },
                pageReference = if (dokumentoversiktBruker.sideInfo.finnesNesteSide) {
                    dokumentoversiktBruker.sideInfo.sluttpeker
                } else {
                    null
                },
                antall = dokumentoversiktBruker.sideInfo.antall,
                totaltAntall = dokumentoversiktBruker.sideInfo.totaltAntall
            )
        } else {
            return DokumenterResponse(dokumenter = emptyList(), pageReference = null, antall = 0, totaltAntall = 0)
        }
    }

    private fun mapTema(temaer: List<Tema>): List<no.nav.klage.oppgave.clients.saf.graphql.Tema> =
        temaer.map { tema -> no.nav.klage.oppgave.clients.saf.graphql.Tema.valueOf(tema.name) }

    fun fetchJournalposterConnectedToBehandling(behandling: Behandling): DokumenterResponse {
        val dokumentReferanser = behandling.saksdokumenter.groupBy { it.journalpostId }.keys
            .mapNotNull { safGraphQlClient.getJournalpostAsSaksbehandler(it) }
            .map { dokumentMapper.mapJournalpostToDokumentReferanse(it, behandling) }
        return DokumenterResponse(
            dokumenter = dokumentReferanser.sortedByDescending { it.registrert },
            pageReference = null,
            antall = dokumentReferanser.size,
            totaltAntall = dokumentReferanser.size
        )
    }

    fun validateJournalpostExists(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun validateJournalpostExistsAsSystembruker(journalpostId: String) {
        try {
            safGraphQlClient.getJournalpostAsSystembruker(journalpostId)
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            null
        } ?: throw JournalpostNotFoundException("Journalpost $journalpostId not found")
    }

    fun fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId: String): List<String> {
        return try {
            val journalpost = safGraphQlClient.getJournalpostAsSystembruker(journalpostId)
            journalpost?.dokumenter?.filter { harArkivVariantformat(it) }?.map { it.dokumentInfoId } ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            emptyList()
        }
    }

    fun fetchDokumentInfoIdForJournalpostAsSaksbehandler(journalpostId: String): List<String> {
        return try {
            val journalpost = safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId)
            journalpost?.dokumenter?.filter { harArkivVariantformat(it) }?.map { it.dokumentInfoId } ?: emptyList()
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            emptyList()
        }
    }

    fun getArkivertDokument(journalpostId: String, dokumentInfoId: String): ArkivertDokument {
        return safRestClient.getDokument(dokumentInfoId, journalpostId)
    }

    fun getDokumentReferanse(journalpostId: String, behandling: Behandling): DokumentReferanse {
        val journalpost = safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId)
            ?: throw RuntimeException("Could not find journalpost with id $journalpostId and behandlingId ${behandling.id}")
        return dokumentMapper.mapJournalpostToDokumentReferanse(journalpost = journalpost, behandling = behandling)
    }

    fun getMainDokumentAsSaksbehandler(journalpostId: String): ArkivertDokument {
        val dokumentInfoId = fetchDokumentInfoIdForJournalpostAsSaksbehandler(journalpostId)
        return getArkivertDokument(journalpostId, dokumentInfoId.first())
    }

    fun getMainDokumentTitleAsSaksbehandler(journalpostId: String): String {
        return try {
            val journalpost = safGraphQlClient.getJournalpostAsSaksbehandler(journalpostId)
            val dokumentVariant = journalpost?.dokumenter?.filter { harArkivVariantformat(it) }
            return dokumentVariant?.first()?.dokumentvarianter?.first()?.filnavn!!
        } catch (e: Exception) {
            logger.warn("Unable to find journalpost $journalpostId", e)
            "Unknown"
        }
    }

    private fun harArkivVariantformat(dokumentInfo: DokumentInfo): Boolean =
        dokumentInfo.dokumentvarianter.any { dv ->
            dv.variantformat == Variantformat.ARKIV
        }

    fun createSaksdokumenterFromJournalpostIdSet(journalpostIdSet: List<String>): MutableSet<Saksdokument> {
        val saksdokumenter: MutableSet<Saksdokument> = mutableSetOf()
        journalpostIdSet.forEach {
            saksdokumenter.addAll(createSaksdokument(it))
        }
        return saksdokumenter
    }

    private fun createSaksdokument(journalpostId: String) =
        fetchDokumentInfoIdForJournalpostAsSystembruker(journalpostId)
            .map { Saksdokument(journalpostId = journalpostId, dokumentInfoId = it) }
}

class DokumentMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
    }

    //TODO: Har ikke tatt høyde for skjerming, ref https://confluence.adeo.no/pages/viewpage.action?pageId=320364687
    fun mapJournalpostToDokumentReferanse(
        journalpost: Journalpost,
        behandling: Behandling
    ): DokumentReferanse {

        val hoveddokument = journalpost.dokumenter?.firstOrNull()
            ?: throw RuntimeException("Could not find hoveddokument for journalpost ${journalpost.journalpostId}")

        val dokumentReferanse = DokumentReferanse(
            tittel = hoveddokument.tittel,
            tema = Tema.fromNavn(journalpost.tema?.name).id,
            temaId = Tema.fromNavn(journalpost.tema?.name).id,
            registrert = journalpost.datoOpprettet.toLocalDate(),
            dokumentInfoId = hoveddokument.dokumentInfoId,
            journalpostId = journalpost.journalpostId,
            harTilgangTilArkivvariant = harTilgangTilArkivvariant(hoveddokument),
            valgt = behandling.saksdokumenter.containsDokument(
                journalpost.journalpostId,
                hoveddokument.dokumentInfoId
            ),
            journalposttype = DokumentReferanse.Journalposttype.valueOf(journalpost.journalposttype!!.name),
            journalstatus = if (journalpost.journalstatus != null) {
                DokumentReferanse.Journalstatus.valueOf(journalpost.journalstatus.name)
            } else null,
            behandlingstema = journalpost.behandlingstema,
            behandlingstemanavn = journalpost.behandlingstemanavn,
            sak = if (journalpost.sak != null) {
                DokumentReferanse.Sak(
                    datoOpprettet = journalpost.sak.datoOpprettet,
                    fagsakId = journalpost.sak.fagsakId,
                    fagsaksystem = journalpost.sak.fagsaksystem,
                    //TODO: Fix kodeverk parsing
                    fagsystemId = journalpost.sak.fagsaksystem
                )
            } else null,
            avsenderMottaker = if (journalpost.avsenderMottaker != null) {
                DokumentReferanse.AvsenderMottaker(
                    id = journalpost.avsenderMottaker.id,
                    type = if (journalpost.avsenderMottaker.type != null) DokumentReferanse.AvsenderMottaker.AvsenderMottakerIdType.valueOf(
                        journalpost.avsenderMottaker.type.name
                    ) else null,
                    navn = journalpost.avsenderMottaker.navn,
                    land = journalpost.avsenderMottaker.land,
                    erLikBruker = journalpost.avsenderMottaker.erLikBruker,

                    )
            } else null,
            journalfoerendeEnhet = journalpost.journalfoerendeEnhet,
            journalfortAvNavn = journalpost.journalfortAvNavn,
            opprettetAvNavn = journalpost.opprettetAvNavn,
            datoOpprettet = journalpost.datoOpprettet,
            relevanteDatoer = journalpost.relevanteDatoer?.map {
                DokumentReferanse.RelevantDato(
                    dato = it.dato,
                    datotype = DokumentReferanse.RelevantDato.Datotype.valueOf(it.datotype.name)
                )
            },
            antallRetur = journalpost.antallRetur?.toInt(),
            tilleggsopplysninger = journalpost.tilleggsopplysninger?.map {
                DokumentReferanse.Tilleggsopplysning(
                    key = it.nokkel,
                    value = it.verdi,
                )
            },
            kanal = DokumentReferanse.Kanal.valueOf(journalpost.kanal.name),
            kanalnavn = journalpost.kanalnavn,
            utsendingsinfo = getUtsendingsinfo(journalpost.utsendingsinfo),
        )

        dokumentReferanse.vedlegg.addAll(getVedlegg(journalpost, behandling))

        return dokumentReferanse
    }

    private fun getUtsendingsinfo(utsendingsinfo: Utsendingsinfo?): DokumentReferanse.Utsendingsinfo? {
        if (utsendingsinfo == null) {
            return null
        }

        return with(utsendingsinfo) {
            DokumentReferanse.Utsendingsinfo(
                epostVarselSendt = if (epostVarselSendt != null) {
                    DokumentReferanse.Utsendingsinfo.EpostVarselSendt(
                        tittel = epostVarselSendt.tittel,
                        adresse = epostVarselSendt.adresse,
                        varslingstekst = epostVarselSendt.varslingstekst,
                    )
                } else null,
                smsVarselSendt = if (smsVarselSendt != null) {
                    DokumentReferanse.Utsendingsinfo.SmsVarselSendt(
                        adresse = smsVarselSendt.adresse,
                        varslingstekst = smsVarselSendt.varslingstekst,
                    )
                } else null,
                fysiskpostSendt = if (fysiskpostSendt != null) {
                    DokumentReferanse.Utsendingsinfo.FysiskpostSendt(
                        adressetekstKonvolutt = fysiskpostSendt.adressetekstKonvolutt,
                    )
                } else null,
                digitalpostSendt = if (digitalpostSendt != null) {
                    DokumentReferanse.Utsendingsinfo.DigitalpostSendt(
                        adresse = digitalpostSendt.adresse,
                    )
                } else null,
            )
        }
    }

    private fun getVedlegg(
        journalpost: Journalpost,
        klagebehandling: Klagebehandling
    ): List<DokumentReferanse.VedleggReferanse> {
        return if ((journalpost.dokumenter?.size ?: 0) > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                    valgt = klagebehandling.saksdokumenter.containsDokument(
                        journalpost.journalpostId,
                        vedlegg.dokumentInfoId
                    )
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun getVedlegg(
        journalpost: Journalpost,
        behandling: Behandling
    ): List<DokumentReferanse.VedleggReferanse> {
        return if ((journalpost.dokumenter?.size ?: 0) > 1) {
            journalpost.dokumenter?.subList(1, journalpost.dokumenter.size)?.map { vedlegg ->
                DokumentReferanse.VedleggReferanse(
                    tittel = vedlegg.tittel,
                    dokumentInfoId = vedlegg.dokumentInfoId,
                    harTilgangTilArkivvariant = harTilgangTilArkivvariant(vedlegg),
                    valgt = behandling.saksdokumenter.containsDokument(
                        journalpost.journalpostId,
                        vedlegg.dokumentInfoId
                    )
                )
            } ?: throw RuntimeException("could not create VedleggReferanser from dokumenter")
        } else {
            emptyList()
        }
    }

    private fun harTilgangTilArkivvariant(dokumentInfo: DokumentInfo?): Boolean =
        dokumentInfo?.dokumentvarianter?.any { dv ->
            dv.variantformat == Variantformat.ARKIV && dv.saksbehandlerHarTilgang
        } == true

    private fun MutableSet<Saksdokument>.containsDokument(journalpostId: String, dokumentInfoId: String) =
        any {
            it.journalpostId == journalpostId && it.dokumentInfoId == dokumentInfoId
        }
}
