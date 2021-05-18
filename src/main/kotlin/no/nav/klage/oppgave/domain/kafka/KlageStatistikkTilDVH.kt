package no.nav.klage.oppgave.domain.kafka

import com.fasterxml.jackson.annotation.JsonFormat
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaFormat
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle
import java.time.LocalDate
import java.time.LocalDateTime

const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS"
const val DATE_FORMAT = "yyyy-MM-dd"

/**
 * Brukes av DVH
 * KlageKvalitetStatistikk er en hendelse i en Sak, knyttet til en konkret behandlingstype (eks. søknad, revurdering, endring, klage).
 * Vi sender dette typisk ved mottak, tildeling og fullføring.
 */
@JsonSchemaTitle("SaksbehandlingKA")
data class KlageStatistikkTilDVH(

    @JsonSchemaDescription("Kode som angir hvilken enhet som er ansvarlig for behandlingen på det gjeldende tidspunktet")
    val ansvarligEnhetKode: String?,

    @JsonSchemaDescription("Kode som angir hvilken type enhetskode det er snakk om, som oftest NORG.")
    val ansvarligEnhetType: String = "NORG",

    @JsonSchemaDescription("Feltet angir hvem som er avsender av dataene, (navnet på kildesystemet).")
    val avsender: String = "Kabal",

    @JsonSchemaDescription("Nøkkel til den aktuelle behandling, som kan identifisere den i kildesystemet.")
    val behandlingId: String?,

    @JsonSchemaDescription("Nøkkel til den aktuelle behandling, som kan identifisere den i Kabal.")
    val behandlingIdKabal: String,

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = DATE_FORMAT
    )
    @JsonSchemaFormat(DATE_FORMAT)
    @JsonSchemaDescription("Når behandlingen startet i KA")
    val behandlingStartetKA: LocalDate?,

    @JsonSchemaDescription("Kode som angir den aktuelle behandlingens tilstand på gjeldende tidspunkt. Ha med alle mulige statuser som er naturlig for det enkelte system/ytelse. Som minimum, angi om saken har følgende status: Registrert, Klar for behandling, Venter på bruker, venter på ekstern (arbeidsgiver, lege etc.), venter på utland, Avsluttet.Her bør det også angis at saken er behandlet av beslutter, men sendt i retur for ny behandling.")
    val behandlingStatus: String,

    @JsonSchemaDescription("Kode som beskriver behandlingen, for eksempel, søknad, revurdering, klage, anke, endring, gjenopptak, tilbakekreving o.l.")
    val behandlingType: String,

    @JsonSchemaDescription("Bruker IDen til den ansvarlige beslutningstageren for saken. Medunderskriver.")
    val beslutter: String?,

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = DATE_TIME_FORMAT
    )
    @JsonSchemaFormat(DATE_TIME_FORMAT)
    @JsonSchemaDescription("Tidspunktet da hendelsen faktisk ble gjennomført eller registrert i kildesystemet. (format:$DATE_TIME_FORMAT) Dette er det tidspunkt der hendelsen faktisk er gjeldende fra. Ved for eksempel patching av data eller oppdatering tilbake i tid, skal tekniskTid være lik endrings tidspunktet, mens funksjonellTid angir tidspunktet da endringen offisielt gjelder fra.")
    val endringstid: LocalDateTime,

    @JsonSchemaDescription("Kommaseparert liste av hjemler.")
    val hjemmel: String,

    @JsonSchemaDescription("Den som sendt inn klagen.")
    val klager: Part,

    @JsonSchemaDescription("Grunn til utfallet, hvis det finnes.")
    val omgjoeringsgrunn: String?,

    @JsonSchemaDescription("F.eks. Foreldrepenger. Det finnes kodeverk for dette.")
    val opprinneligFagsaksystem: String,

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = DATE_FORMAT
    )
    @JsonSchemaFormat(DATE_FORMAT)
    @JsonSchemaDescription("Når KA mottok oversendelsen.")
    val overfoertKA: LocalDate,

    /**
     * Eventuell liste? Kodeverk?
     */
    @JsonSchemaDescription("Utfall.")
    val resultat: String?,

    //Hvis EØS kommer tilbake så legg til dette.
//    /**
//    "maxLength": 100 ,
//    "description": "Kode som beskriver behandlingens utlandstilsnitt i henhold til NAV spesialisering. I hoved sak vil denne koden beskrive om saksbehandlingsfrister er i henhold til utlandssaker eller innlandssaker, men vil for mange kildesystem være angitt med en høyere oppløsning."
//     */
//    val utenlandstilsnitt: String,

    @JsonSchemaDescription("Den som har rettigheten.")
    val sakenGjelder: Part,

    @JsonSchemaDescription("Bruker IDen til saksbehandler ansvarlig for saken på gjeldende tidspunkt. Kan etterlates tom ved helautomatiske delprosesser i behandlingen. Bør bare fylles når det er manuelle skritt i saksbehandlingen som utføres.")
    val saksbehandler: String?,

    @JsonSchemaDescription("Enhet til gjeldende saksbehandler.")
    val saksbehandlerEnhet: String?,

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = DATE_TIME_FORMAT
    )
    @JsonSchemaFormat(DATE_TIME_FORMAT)
    @JsonSchemaDescription("Tidspunktet da kildesystemet ble klar over hendelsen. (format:$DATE_TIME_FORMAT). Dette er tidspunkt hendelsen ble endret i dato systemet. Sammen med funksjonellTid, vil vi kunne holde rede på hva som er blitt rapportert tidligere og når det skjer endringer tilbake i tid.")
    val tekniskTid: LocalDateTime,

    @JsonSchemaDescription("Nøkkel til det aktuelle vedtaket da behandlingen blir tilknyttet et slikt. Vi skal helst kunne identifisere vedtaket i kildensystemet.")
    val vedtakId: String,

    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = DATE_FORMAT
    )
    @JsonSchemaFormat(DATE_FORMAT)
    @JsonSchemaDescription("Dato for vedtaket i KA.")
    val vedtaksdato: LocalDate?,

    @JsonSchemaDescription("Angir på hvilken versjon av kildekoden JSON stringen er generert på bakgrunn av.")
    val versjon: Int = 1,

    /**
    DVH har et eget kodeverk for disse. Kan være vi kan bruke dette.
     */
    @JsonSchemaDescription("Stønaden eller ytelsen det er saken omhandler. Hva gjelder saken?")
    val ytelseType: String
) {
    data class Part(
        val verdi: String,
        val type: PartIdType
    )

    enum class PartIdType {
        PERSON, VIRKSOMHET
    }
}
