package no.nav.klage.oppgave.domain.klage

import java.time.LocalDateTime
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "endring", schema = "klage")
class Endringslogginnslag(
    @Column(name = "saksbehandlerident")
    val saksbehandlerident: String?, //subjekt?
    @Column(name = "kilde")
    val kilde: KildeSystem,
    @Column(name = "handling")
    val handling: Handling,
    @Column(name = "felt")
    val felt: String,
    @Column(name = "fraverdi")
    val fraVerdi: String?,
    @Column(name = "tilverdi")
    val tilVerdi: String?,
    @Column(name = "klagebehandling_id")
    val klagebehandlingId: UUID,
    @Id
    val id: UUID = UUID.randomUUID(),
    @Column(name = "tidspunkt")
    val tidspunkt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun endringFromKabal(
            saksbehandlerident: String?,
            felt: String,
            fraVerdi: String,
            tilVerdi: String,
            klagebehandlingId: UUID
        ): Endringslogginnslag {
            return Endringslogginnslag(
                saksbehandlerident = saksbehandlerident,
                kilde = KildeSystem.KABAL,
                handling = Handling.ENDRING,
                felt = felt,
                fraVerdi = fraVerdi,
                tilVerdi = tilVerdi,
                klagebehandlingId = klagebehandlingId
            )
        }

        fun opprettingFromKabal(
            saksbehandlerident: String?,
            felt: String,
            tilVerdi: String,
            klagebehandlingId: UUID
        ): Endringslogginnslag {
            return Endringslogginnslag(
                saksbehandlerident = saksbehandlerident,
                kilde = KildeSystem.KABAL,
                handling = Handling.NY,
                felt = felt,
                fraVerdi = null,
                tilVerdi = tilVerdi,
                klagebehandlingId = klagebehandlingId
            )
        }

        fun slettingFromKabal(
            saksbehandlerident: String?,
            felt: String,
            fraVerdi: String,
            klagebehandlingId: UUID
        ): Endringslogginnslag {
            return Endringslogginnslag(
                saksbehandlerident = saksbehandlerident,
                kilde = KildeSystem.KABAL,
                handling = Handling.SLETTING,
                felt = felt,
                fraVerdi = fraVerdi,
                tilVerdi = null,
                klagebehandlingId = klagebehandlingId
            )
        }

    }
}

enum class Handling {
    NY, ENDRING, SLETTING
}

enum class KildeSystem {
    KABAL, OPPGAVE
}
