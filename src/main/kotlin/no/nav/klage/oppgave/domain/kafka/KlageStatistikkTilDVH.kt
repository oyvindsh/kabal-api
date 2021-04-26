package no.nav.klage.oppgave.domain.kafka

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Used by DVH
 * KlageKvalitetStatistikk er en avgjørelse i en Sak, knyttet til en konkret behandlingstype (eks. søknad, revurdering, endring, klage)
 */
data class KlageStatistikkTilDVH(
    /**
    "Tidspunktet da hendelsen faktisk ble gjennomført eller registrert i kildesystemet. (format:yyyy-mm-ddThh24:mn:ss.FF6) Dette er det tidspunkt der hendelsen faktisk er gjeldende fra. Ved for eksempel patching av data eller oppdatering tilbake i tid, skal tekniskTid være lik endrings tidspunktet, mens funksjonellTid angir tidspunktet da endringen offisielt gjelder fra."
     */
    val funksjonellTid: LocalDateTime,

    /**
    Tidspunktet da kildesystemet ble klar over hendelsen. (format:yyyy-mm-ddThh24:mn:ss.FF6). Dette er tidspunkt hendelsen ble endret i dato systemet. Sammen med funksjonellTid, vil vi kunne holde rede på hva som er blitt rapportert tidligere og når det skjer endringer tilbake i tid.
     */
    val tekniskTid: LocalDateTime,

    /**
    Denne datoen forteller fra hvilken dato behandlingen først ble initiert. Datoen brukes i beregning av saksbehandlingstid og skal samsvare med brukerens opplevelse av at saksbehandlingen har startet.
     */
    val mottattDato: LocalDate,

    /**
    Tidspunkt for når behandlingen ble registrert i saksbehandlingssystemet. Denne kan avvike fra mottattDato hvis det tar tid fra postmottak til registrering i system, eller hvis en oppgave om å opprette behandling ligger på vent et sted i NAV. Ved automatisk registrering av saker er denne samme som mottattDato.
     */
    val registrertDato: LocalDate,

    /**
    Tidspunkt for når søknaden er komplett og saksbehandling kan starte.
     */
    val komplettSoknadDato: LocalDate,

    /**
    "maxLength": 40 ,
    "description":"Aktør IDen til primær mottager av ytelsen om denne blir godkjent. Altså, den som saken omhandler."
     */
    val aktorId: Long,

    /**
    Antall gjenstående dager i rettighetsperioden.
     */
    val resterendeDager: Int,

    /**
    "maxLength": 100,
    "description": "Stønaden eller ytelsen det er saken omhandler. Hva gjelder saken?"
     */
    val ytelseType: String,

    /**
    "maxLength": 100,
    "description": "Søkertype. F.eks arbeidstaker eller selvstendig næringsdrivende."
     */
    val sokerType: String,

    /**
    "maxLength": 40 ,
    "description": "Nøkkel til den aktuelle behandling, som kan identifiserer den i kildensystemet."
     */
    val behandlingId: String,

    /**
    "maxLength": 40 ,
    "description": "Saksnummeret tilknyttet saken."
     */
    val saksnummer: String,

    /**
    Tidspunkt da vedtaket på behandlingen falt.
     */
    val vedtaksDato: LocalDate,

    /**
    "maxLength": 40 ,
    "description":"Hvis behandlingen oppstår som resultat av en tidligere behandling, skal det refereres til denne behandlingen. Eksempel gjelder dette ved revurdering eller klage, hvor det skal vises til opprinnelig behandling med aktuelt vedtak."
     */
    val relatertBehandlingId: String,

    /**
    "maxLength": 40 ,
    "description": "Nøkkel til det aktuelle vedtaket da behandlingen blir tilknyttet et slikt. Vi skal helst kunne identifisere vedtaket i kildensystemet."
     */
    val vedtakId: String,

    /**
    "maxLength": 100 ,
    "description":"Kode som beskriver behandlingen, for eksempel, søknad, revurdering, klage, anke, endring, gjenopptak, tilbakekreving o.l."
     */
    val behandlingType: String,

    /**
    "maxLength": 100 ,
    "description": "Kode som angir den aktuelle behandlingens tilstand på gjeldende tidspunkt. Ha med alle mulige statuser som er naturlig for det enkelte system/ytelse. Som minimum, angi om saken har følgende status: Registrert, Klar for behandling, Venter på bruker, venter på ekstern (arbeidsgiver, lege etc.), venter på utland, Avsluttet.Her bør det også angis at saken er behandlet av beslutter, men sendt i retur for ny behandling."
     */
    val behandlingStatus: String,

    /**
    "maxLength": 100 ,
    "description":"Kode som angir resultat av behandling på innværende tidspunkt. Mulige verdier: innvilget (delvis innvilget), avslått, omgjort, feilregistrert, henlagt, trukket, avvist etc."
     */
    val resultat: String,

    /**
    "maxLength": 100 ,
    "description": "Denne må inneholde en årsaksbeskrivelse knyttet til et hvert mulig resultat av behandlingen. Den kan enten være underordnet resultat eller stå for seg selv. Eks. årsak til avslag, årsak til delvis innvilgelse."
     */
    val resultatBegrunnelse: String,

    /**
    "maxLength": 100 ,
    "description": "Kode som beskriver behandlingens utlandstilsnitt i henhold til NAV spesialisering. I hoved sak vil denne koden beskrive om saksbehandlingsfrister er i henhold til utlandssaker eller innlandssaker, men vil for mange kildesystem være angitt med en høyere oppløsning."
     */
    val utenlandstilsnitt: String,

    /**
    "maxLength": 1000 ,
    "description": "Beskriver den funksjonelle verdien av koden. Finnes ikke den enda, regner vi med å få den senere."
     */
    val behandlingTypeBeskrivelse: String,

    /**
    "maxLength": 1000 ,
    "description": "Beskriver den funksjonelle verdien av koden. Finnes ikke den enda, regner vi med å få den senere."
     */
    val behandlingStatusBeskrivelse: String,

    /**
    "maxLength": 1000 ,
    "description": "Beskriver den funksjonelle verdien av koden. Finnes ikke den enda, regner vi med å få den senere."
     */
    val resultatBeskrivelse: String,

    /**
    "maxLength": 1000 ,
    "description": "Beskriver den funksjonelle verdien av koden. Finnes ikke den enda, regner vi med å få den senere."
     */
    val resultatBegrunnelseBeskrivelse: String,

    /**
    "maxLength": 1000 ,
    "description": "Beskriver den funksjonelle verdien av koden. Finnes ikke den enda, regner vi med å få den senere."
     */
    val utenlandstilsnittBeskrivelse: String,

    /**
    "maxLength": 40 ,
    "description": "Bruker IDen til den ansvarlige beslutningstageren for saken."
     */
    val beslutter: String,

    /**
    "maxLength": 40 ,
    "description": "Bruker IDen til saksbehandler ansvarlig for saken på gjeldende tidspunkt. Kan etterlates tom ved helautomatiske delprosesser i behandlingen. Bør bare fylles når det er manuelle skritt i saksbehandlingen som utføres."
     */
    val saksbehandler: String,

    /**
    "maxLength": 40 ,
    "description": "Opprinnelsen til behandlingen. Mulige verdier: AktørID, saksbehandlerID, system (automatisk)"
     */
    val behandlingOpprettetAv: String,

    /**
    "maxLength": 100 ,
    "description":"Beskriver den funksjonelle verdien av koden. Finnes ikke den enda, regner vi med å få den senere."
     */
    val behandlingOpprettetType: String,

    /**
    "maxLength": 1000 ,
    "description": "Kode som beskriver behandlingsens utlandstilsnitt i henhold til NAV spesialisering. I hoved sak vil denne koden beskrive om saksbehandlingsfrister er i henhold til utlandssaker eller innlandssaker, men vil for mange kildesystem være angitt med en høyere oppløsning."
     */
    val behandlingOpprettetTypeBeskrivelse: String,

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
    "maxLength": 10 ,
    "description": "Kode som angir hvilken enhet som faktisk utfører behandlingen på det gjeldende tidspunktet."
     */
    val behandlendeEnhetKode: String,

    /**
    "maxLength": 100 ,
    "description": "Kode som angir hvilken type enhetskode det er snakk om, som oftest NORG. Kan også angi en automatisk prosess."
     */
    val behandlendeEnhetType: String,

    /**
    Den forespeilede datoen for når stønaden/ytelsen betales ut. Eks. Foreldrepenger er uttaksdato første utbetaling etter at foreldrepengeperioden har startet, ved Pensjon er uttaksdato tidspunktet for første pensjonsutbetaling.
     */
    val datoForUttak: LocalDate,

    /**
    Den faktiske datoen for når stønaden/ytelsen betales ut til bruker.
     */
    val datoForUtbetaling: LocalDate,

    /**
    Behandlingen krever totrinnsbehandling.
     */
    val totrinnsbehandling: Boolean,

    /**
    True hvis behandlingen er 100% automatisk behandlet (urørt av menneskehender).
     */
    val automatiskbehandling: Boolean,

    /**
    "maxLength": 100 ,
    "description":"Kode som angir hvem det utbetales til."
     */
    val utbetalesTil: String,

    /**
    "maxLength": 40 ,
    "description":"Feltet angir hvem som er avsender av dataene, (navnet på kildesystemet)."
     */
    val avsender: String,

    /**
    Angir på hvilken versjon av kildekoden JSON stringen er generert på bakgrunn av.
     */
    val versjon: Int,
)
