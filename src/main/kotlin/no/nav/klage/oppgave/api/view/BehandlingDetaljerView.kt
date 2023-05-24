package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class BehandlingDetaljerView(
    val id: UUID,
    val fraNAVEnhet: String?,
    val fraNAVEnhetNavn: String?,
    val mottattVedtaksinstans: LocalDate? = null,
    val sakenGjelder: SakenGjelderView,
    val klager: PartView,
    val prosessfullmektig: PartView?,
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
    val sattPaaVent: LocalDateTime? = null,
    val sendtTilTrygderetten: LocalDateTime? = null,
    val kjennelseMottatt: LocalDateTime? = null,
    val feilregistrering: FeilregistreringView? = null,
) {

    data class KvalitetsvurderingReference(
        val id: UUID?,
        val version: Int,
    )

    data class FeilregistreringView(
        val feilregistrertAv: SaksbehandlerView,
        val registered: LocalDateTime,
        val reason: String,
        val fagsystemId: String,
    )

    interface PartBase {
        val id: String
        val name: String?
    }

    enum class Sex {
        MANN, KVINNE, UKJENT
    }

    enum class IdType {
        FNR, ORGNR
    }

    interface IdPart {
        val type: IdType
    }

    data class PartView(
        override val id: String,
        override val name: String?,
        override val type: IdType
    ): PartBase, IdPart

    data class SakenGjelderView(
        override val id: String,
        override val name: String?,
        override val type: IdType,
        val sex: Sex,
    ): PartBase, IdPart
}