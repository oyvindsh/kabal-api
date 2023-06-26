package no.nav.klage.oppgave.clients.kabaldocument

import no.nav.klage.dokument.domain.dokumenterunderarbeid.DokumentUnderArbeid
import no.nav.klage.kodeverk.Brevmottakertype
import no.nav.klage.kodeverk.DokumentType
import no.nav.klage.kodeverk.PartIdType
import no.nav.klage.oppgave.clients.ereg.EregClient
import no.nav.klage.oppgave.clients.kabaldocument.model.request.*
import no.nav.klage.oppgave.clients.pdl.PdlFacade
import no.nav.klage.oppgave.domain.klage.Behandling
import no.nav.klage.oppgave.domain.klage.PartId
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class KabalDocumentMapper(
    private val pdlFacade: PdlFacade,
    private val eregClient: EregClient
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
        vedlegg: SortedSet<DokumentUnderArbeid>
    ): DokumentEnhetWithDokumentreferanserInput {
        return DokumentEnhetWithDokumentreferanserInput(
            brevMottakere = mapBrevmottakere(behandling, hovedDokument.brevmottakertyper, hovedDokument.dokumentType!!),
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
                vedlegg = vedlegg.filter { it.getType() != DokumentUnderArbeid.DokumentUnderArbeidType.JOURNALFOERT }
                    .map { mapDokumentUnderArbeidToDokumentReferanse(it) },
                journalfoerteVedlegg = vedlegg.filter { it.getType() == DokumentUnderArbeid.DokumentUnderArbeidType.JOURNALFOERT }
                    .map {
                        DokumentEnhetWithDokumentreferanserInput.DokumentInput.JournalfoertDokument(
                            kildeJournalpostId = it.journalfoertDokumentReference!!.journalpostId,
                            dokumentInfoId = it.journalfoertDokumentReference.dokumentInfoId
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

    fun mapBrevmottakere(
        behandling: Behandling,
        brevMottakertyper: Set<Brevmottakertype>,
        dokumentType: DokumentType
    ): List<BrevmottakerInput> {
        val brevmottakerPartIds = mutableSetOf<PartId>()

        if (dokumentType == DokumentType.NOTAT) {
            brevmottakerPartIds.add(behandling.sakenGjelder.partId)
        } else {
            if (brevMottakertyper.contains(Brevmottakertype.PROSESSFULLMEKTIG)) {
                brevmottakerPartIds.add(behandling.klager.prosessfullmektig!!.partId)
            }

            if (brevMottakertyper.contains(Brevmottakertype.KLAGER)) {
                brevmottakerPartIds.add(behandling.klager.partId)
            }

            if (brevMottakertyper.contains(Brevmottakertype.SAKEN_GJELDER)) {
                brevmottakerPartIds.add(behandling.sakenGjelder.partId)
            }
        }

        return brevmottakerPartIds.map { mapPartIdToBrevmottakerInput(it) }
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