package no.nav.klage.oppgave.api.view

import no.nav.klage.kodeverk.MedunderskriverFlyt
import no.nav.klage.oppgave.domain.klage.SattPaaVent
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
    val temaId: String,
    val ytelseId: String,
    val typeId: String,
    val mottattKlageinstans: LocalDate,
    val tildelt: LocalDate? = null,
    val avsluttetAvSaksbehandlerDate: LocalDate?,
    val isAvsluttetAvSaksbehandler: Boolean,
    val frist: LocalDate? = null,
    val tildeltSaksbehandlerident: String? = null,
    val tildeltSaksbehandlerEnhet: String? = null,
    val medunderskriverident: String? = null,
    val medunderskriverFlyt: MedunderskriverFlyt,
    val datoSendtMedunderskriver: LocalDate?,
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
    val dead: LocalDate?,
    val fullmakt: Boolean,
    val kvalitetsvurderingReference: KvalitetsvurderingReference?,
    val sattPaaVent: SattPaaVent? = null,
    val sendtTilTrygderetten: LocalDateTime? = null,
    val kjennelseMottatt: LocalDateTime? = null,
    val feilregistrering: FeilregistreringView? = null,
    val fagsystemId: String,
    val rol: ROLView?,
    val relevantDocumentIdList: Set<String>,
) {

    data class ROLView(
        val rol: SaksbehandlerView?,
        val rolStateId: String,
    )

    data class KvalitetsvurderingReference(
        val id: UUID,
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
        val available: Boolean
        val statusList: List<PartStatus>
    }

    data class PartStatus(
        val status: Status,
        val date: LocalDate? = null,
    ) {
        enum class Status {
            DEAD,
            DELETED,
            FORTROLIG,
            STRENGT_FORTROLIG,
            EGEN_ANSATT,
            VERGEMAAL,
            FULLMAKT,
        }
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
        override val type: IdType,
        override val available: Boolean,
        override val statusList: List<PartStatus>,
    ): PartBase, IdPart

    data class SakenGjelderView(
        override val id: String,
        override val name: String?,
        override val type: IdType,
        override val available: Boolean,
        override val statusList: List<PartStatus>,
        val sex: Sex,
    ): PartBase, IdPart
}