package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class BehandlingDetaljerView(
    val id: UUID,
    val klageInnsendtdato: LocalDate?,
    val fraNAVEnhet: String?,
    val fraNAVEnhetNavn: String?,
    val forrigeNAVEnhet: String? = null,
    val forrigeNAVEnhetNavn: String? = null,
    val mottattVedtaksinstans: LocalDate? = null,
    val sakenGjelder: SakenGjelderView,
    val klager: KlagerView,
    val prosessfullmektig: ProsessfullmektigView?,
    val tema: String,
    val temaId: String,
    val ytelse: String? = null,
    val ytelseId: String,
    val type: String,
    val typeId: String,
    val mottatt: LocalDate?,
    val mottattKlageinstans: LocalDate,
    val tildelt: LocalDate? = null,
    val avsluttetAvSaksbehandlerDate: LocalDate?,
    val isAvsluttetAvSaksbehandler: Boolean,
    val frist: LocalDate? = null,
    val tildeltSaksbehandlerident: String? = null,
    val tildeltSaksbehandler: SaksbehandlerView? = null,
    val tildeltSaksbehandlerEnhet: String? = null,
    val medunderskriverident: String? = null,
    val medunderskriver: SaksbehandlerView? = null,
    val medunderskriverFlyt: MedunderskriverFlyt,
    val datoSendtMedunderskriver: LocalDate?,
    val hjemler: List<String>,
    val hjemmelIdList: List<String>,
    val modified: LocalDateTime,
    val created: LocalDateTime,
    val fraSaksbehandlerident: String? = null,
    val forrigeSaksbehandlerident: String? = null,
    val forrigeVedtaksDato: LocalDate? = null,
    val resultat: VedtakView?,
    val kommentarFraVedtaksinstans: String?,
    val tilknyttedeDokumenter: Set<TilknyttetDokument>,
    val egenAnsatt: Boolean,
    val fortrolig: Boolean,
    val strengtFortrolig: Boolean,
    val vergemaalEllerFremtidsfullmakt: Boolean,
    //TODO can be deleted?
    val kvalitetsvurderingId: UUID? = null,
    //TODO make nullable
    val kvalitetsvurderingReference: KvalitetsvurderingReference,
    val isPossibleToUseDokumentUnderArbeid: Boolean = false,
    val sattPaaVent: LocalDateTime? = null,
    val sendtTilTrygderetten: LocalDateTime? = null,
    val kjennelseMottatt: LocalDateTime? = null,
    val feilregistrering: FeilregistreringView? = null,
) {
    data class NavnView(
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
    )

    data class KlagerView(
        val person: PersonView?,
        val virksomhet: VirksomhetView?
    )

    data class ProsessfullmektigView(
        val person: PersonView?,
        val virksomhet: VirksomhetView?
    )

    data class SakenGjelderView(
        val person: PersonView?,
        val virksomhet: VirksomhetView?
    )

    data class VirksomhetView(
        val virksomhetsnummer: String?,
        val navn: String?,
    )

    data class PersonView(
        val foedselsnummer: String?,
        val navn: NavnView?,
        val kjoenn: String?,
    )

    data class KvalitetsvurderingReference(
        val id: UUID?,
        val version: Int,
    )

    data class FeilregistreringView(
        val navIdent: String,
        val registered: LocalDateTime,
        val reason: String,
        val fagsystemId: String,
    )
}
