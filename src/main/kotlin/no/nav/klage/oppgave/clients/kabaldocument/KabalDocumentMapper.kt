package no.nav.klage.oppgave.clients.kabaldocument

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.dokument.domain.dokumenterunderarbeid.Innholdsfortegnelse
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.kabaldocument.model.request.*
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.clients.saf.graphql.SafGraphQlClient
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getPartIdFromIdentifikator
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class KabalDocumentMapper(
    private val pdlFacade: PdlFacade,
    private val eregClient: EregClient,
    private val safClient: SafGraphQlClient,
) {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()

        private const val BREVKODE = "BREV_FRA_KLAGEINSTANS"
        private const val BREVKODE_NOTAT = "NOTAT_FRA_KLAGEINSTANS"
        private const val BEHANDLINGSTEMA_KLAGE_KLAGEINSTANS = "ab0164"
        private const val KLAGEBEHANDLING_ID_KEY = "klagebehandling_id"
    }

    fun mapBehandlingToDokumentEnhetWithDokumentreferanser(
        behandling: Behandling,
        hovedDokument: DokumentUnderArbeid,
        vedlegg: SortedSet<DokumentUnderArbeid>,
        innholdsfortegnelse: Innholdsfortegnelse?,
    ): DokumentEnhetWithDokumentreferanserInput {

        val innholdsfortegnelseDocument = if (vedlegg.size > 1) {
            DokumentEnhetWithDokumentreferanserInput.DokumentInput.Dokument(
                mellomlagerId = innholdsfortegnelse?.mellomlagerId!!,
                opplastet = innholdsfortegnelse.modified,
                size = 0,
                name = "Innholdsfortegnelse"
            )
        } else null

        val vedleggMapped = vedlegg.filter { it.getType() != DokumentUnderArbeid.DokumentUnderArbeidType.JOURNALFOERT }
            .sortedByDescending { it.created }
            .map { currentVedlegg ->
                mapDokumentUnderArbeidToDokumentReferanse(
                    dokument = currentVedlegg,
                )
            }.toMutableList()
        if (innholdsfortegnelseDocument != null) {
            vedleggMapped.add(0, innholdsfortegnelseDocument)
        }

        val journalfoerteVedlegg =
            vedlegg.filter { it.getType() == DokumentUnderArbeid.DokumentUnderArbeidType.JOURNALFOERT }
                .sortedWith { document1, document2 ->
                    val journalpostInDokarkiv1 =
                        safClient.getJournalpostAsSystembruker(document1.journalfoertDokumentReference!!.journalpostId)

                    val journalpostInDokarkiv2 =
                        safClient.getJournalpostAsSystembruker(document2.journalfoertDokumentReference!!.journalpostId)

                    val dokumentInDokarkiv1 =
                        journalpostInDokarkiv1.dokumenter?.find { it.dokumentInfoId == document1.journalfoertDokumentReference.dokumentInfoId }
                            ?: throw RuntimeException("Document not found in Dokarkiv")

                    val dokumentInDokarkiv2 =
                        journalpostInDokarkiv2.dokumenter?.find { it.dokumentInfoId == document2.journalfoertDokumentReference.dokumentInfoId }
                            ?: throw RuntimeException("Document not found in Dokarkiv")

                    val dateCompare =
                        journalpostInDokarkiv2.datoOpprettet.compareTo(journalpostInDokarkiv1.datoOpprettet)
                    if (dateCompare != 0) {
                        dateCompare
                    } else {
                        (dokumentInDokarkiv1.tittel ?: "Tittel ikke funnet i SAF").compareTo(
                            dokumentInDokarkiv2.tittel ?: "Tittel ikke funnet i SAF"
                        )
                    }
                }

        return DokumentEnhetWithDokumentreferanserInput(
            brevMottakere = mapBrevmottakerIdentToBrevmottakerInput(
                behandling,
                hovedDokument.brevmottakerIdents,
                hovedDokument.dokumentType!!
            ),
            journalfoeringData = JournalfoeringDataInput(
                sakenGjelder = PartIdInput(
                    partIdTypeId = behandling.sakenGjelder.partId.type.id,
                    value = behandling.sakenGjelder.partId.value
                ),
                temaId = behandling.ytelse.toTema().id,
                sakFagsakId = behandling.fagsakId,
                sakFagsystemId = behandling.fagsystem.id,
                kildeReferanse = behandling.id.toString(),
                enhet = behandling.tildeling!!.enhet!!,
                behandlingstema = BEHANDLINGSTEMA_KLAGE_KLAGEINSTANS,
                //Tittel gjelder journalposten, ikke selve dokumentet som lastes opp. Vises i Gosys.
                tittel = hovedDokument.dokumentType!!.beskrivelse,
                brevKode = if (hovedDokument.dokumentType == DokumentType.NOTAT) BREVKODE_NOTAT else BREVKODE,
                tilleggsopplysning = TilleggsopplysningInput(
                    key = KLAGEBEHANDLING_ID_KEY,
                    value = behandling.id.toString()
                )
            ),
            dokumentreferanser = DokumentEnhetWithDokumentreferanserInput.DokumentInput(
                hoveddokument = mapDokumentUnderArbeidToDokumentReferanse(hovedDokument),
                vedlegg = vedleggMapped,
                journalfoerteVedlegg = journalfoerteVedlegg
                    .map { currentVedlegg ->
                        DokumentEnhetWithDokumentreferanserInput.DokumentInput.JournalfoertDokument(
                            kildeJournalpostId = currentVedlegg.journalfoertDokumentReference!!.journalpostId,
                            dokumentInfoId = currentVedlegg.journalfoertDokumentReference.dokumentInfoId,
                        )
                    },
            ),
            dokumentTypeId = hovedDokument.dokumentType!!.id,
            journalfoerendeSaksbehandlerIdent = hovedDokument.markertFerdigBy!!
        )
    }

    private fun mapDokumentUnderArbeidToDokumentReferanse(dokument: DokumentUnderArbeid): DokumentEnhetWithDokumentreferanserInput.DokumentInput.Dokument {
        return DokumentEnhetWithDokumentreferanserInput.DokumentInput.Dokument(
            mellomlagerId = dokument.mellomlagerId!!,
            opplastet = dokument.opplastet!!,
            size = dokument.size!!,
            name = dokument.name,
        )
    }

    fun mapBrevmottakerIdentToBrevmottakerInput(
        behandling: Behandling,
        brevmottakerIdents: Set<String>?,
        dokumentType: DokumentType
    ): List<BrevmottakerInput> {
        return if (dokumentType == DokumentType.NOTAT) {
            listOf(mapPartIdToBrevmottakerInput(behandling.sakenGjelder.partId))
        } else {
            brevmottakerIdents!!.map {
                mapPartIdToBrevmottakerInput(getPartIdFromIdentifikator(it))
            }
        }
    }

    private fun mapPartIdToBrevmottakerInput(partId: PartId) =
        BrevmottakerInput(
            partId = mapPartId(partId),
            navn = getNavn(partId),
        )


    private fun mapPartId(partId: PartId): PartIdInput =
        PartIdInput(
            partIdTypeId = partId.type.id,
            value = partId.value
        )

    private fun getNavn(partId: PartId): String =
        if (partId.type == PartIdType.PERSON) {
            pdlFacade.getPersonInfo(partId.value).settSammenNavn()
        } else {
            eregClient.hentOrganisasjon(partId.value).navn.sammensattnavn
        }

}