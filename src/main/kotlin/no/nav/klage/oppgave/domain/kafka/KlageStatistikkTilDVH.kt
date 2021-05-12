package no.nav.klage.oppgave.domain.kafka

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Used by DVH
 * KlageKvalitetStatistikk er en avgjørelse i en Sak, knyttet til en konkret behandlingstype (eks. søknad, revurdering, endring, klage)
 */
data class KlageStatistikkTilDVH(
    /**
    "maxLength": 40 ,
    "description":"Aktør IDen til primær mottager av ytelsen om denne blir godkjent. Altså, den som saken omhandler."
     */
    val aktorId: Long,

    /**
    "maxLength": 10 ,
    "description":"Kode som angir hvilken enhet som er ansvarlig for behandlingen på det gjeldende tidspunktet."
     */
    val ansvarligEnhetKode: String,

    /**
    "maxLength": 100 ,
    "description": "Kode som angir hvilken type enhetskode det er snakk om, som oftest NORG."
     */
    val ansvarligEnhetType: String,

    /**
    "maxLength": 40 ,
    "description":"Feltet angir hvem som er avsender av dataene, (navnet på kildesystemet)."
     */
    val avsender: String,

    /**
    "maxLength": 40 ,
    "description": "Nøkkel til den aktuelle behandling, som kan identifiserer den i kildensystemet."
     */
    val behandlingId: String,

    /**
    "Når behandlingen startet i KA"
     */
    val behandlingStartetKA: LocalDate,

    /**
    "maxLength": 100 ,
    "description": "Kode som angir den aktuelle behandlingens tilstand på gjeldende tidspunkt. Ha med alle mulige statuser som er naturlig for det enkelte system/ytelse. Som minimum, angi om saken har følgende status: Registrert, Klar for behandling, Venter på bruker, venter på ekstern (arbeidsgiver, lege etc.), venter på utland, Avsluttet.Her bør det også angis at saken er behandlet av beslutter, men sendt i retur for ny behandling."
     */
    val behandlingStatus: String,

    /**
    "maxLength": 100 ,
    "description":"Kode som beskriver behandlingen, for eksempel, søknad, revurdering, klage, anke, endring, gjenopptak, tilbakekreving o.l."
     */
    val behandlingType: String,

    /**
    "maxLength": 40 ,
    "description": "Bruker IDen til den ansvarlige beslutningstageren for saken. Medunderskriver."
     */
    val beslutter: String,

    /**
    "Tidspunktet da hendelsen faktisk ble gjennomført eller registrert i kildesystemet. (format:yyyy-mm-ddThh24:mn:ss.FF6) Dette er det tidspunkt der hendelsen faktisk er gjeldende fra. Ved for eksempel patching av data eller oppdatering tilbake i tid, skal tekniskTid være lik endrings tidspunktet, mens funksjonellTid angir tidspunktet da endringen offisielt gjelder fra."
     */
    val endringstid: LocalDateTime,

    /**
    "description": "Liste pr utfall."
     */
    val hjemmel: String,

    /**
     * Den som sendt inn klagen
    TODO: Type som fnr/virksomhetsnr
     */
    val klager: String,

    val omgjoeringsgrunn: String,

    /**
     * f.eks. Foreldrepenger. Det finnes kodeverk for dette.
     */
    val opprinneligFagsaksystem: String,

    val overfoertKA: LocalDate,



    //DVH har et eget kodeverk for disse. Kan være vi kan bruke dette.
//    /**
//    "maxLength": 100,
//    "description": "Stønaden eller ytelsen det er saken omhandler. Hva gjelder saken?"
//     */
//    val ytelseType: String,

    /**
    Tidspunkt da vedtaket på behandlingen falt.
     */
    val vedtaksDato: LocalDate,

    /**
     * Utfall.
     * Eventuell liste? Kodeverk?
     */
    val resultat: String,

    //Hvis EØS kommer tilbake så legg til dette.
//    /**
//    "maxLength": 100 ,
//    "description": "Kode som beskriver behandlingens utlandstilsnitt i henhold til NAV spesialisering. I hoved sak vil denne koden beskrive om saksbehandlingsfrister er i henhold til utlandssaker eller innlandssaker, men vil for mange kildesystem være angitt med en høyere oppløsning."
//     */
//    val utenlandstilsnitt: String,

    /**
     * Den som har rettigheten
    TODO: Type som fnr/virksomhetsnr
     */
    val sakenGjelder: String,

    /**
    "maxLength": 40 ,
    "description": "Bruker IDen til saksbehandler ansvarlig for saken på gjeldende tidspunkt. Kan etterlates tom ved helautomatiske delprosesser i behandlingen. Bør bare fylles når det er manuelle skritt i saksbehandlingen som utføres."
     */
    val saksbehandler: String,

    val saksbehandlerEnhet: String,

    /**
    Tidspunktet da kildesystemet ble klar over hendelsen. (format:yyyy-mm-ddThh24:mn:ss.FF6). Dette er tidspunkt hendelsen ble endret i dato systemet. Sammen med funksjonellTid, vil vi kunne holde rede på hva som er blitt rapportert tidligere og når det skjer endringer tilbake i tid.
     */
    val tekniskTid: LocalDateTime,

    /**
    "maxLength": 40 ,
    "description": "Nøkkel til det aktuelle vedtaket da behandlingen blir tilknyttet et slikt. Vi skal helst kunne identifisere vedtaket i kildensystemet."
     */
    val vedtakId: String,

    /**
     * Dato for vedtaket i KA
     */
    val vedtaksdato: LocalDate,

    /**
    Angir på hvilken versjon av kildekoden JSON stringen er generert på bakgrunn av.
     */
    val versjon: Int,
)
