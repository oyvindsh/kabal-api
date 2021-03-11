package no.nav.klage.oppgave.api.mapper


import no.nav.klage.oppgave.api.internal.OppgaveKopiAPIModel
import no.nav.klage.oppgave.domain.oppgavekopi.*
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Service

@Service
class OppgaveMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapOppgaveKopiAPIModelToOppgaveKopi(oppgave: OppgaveKopiAPIModel): OppgaveKopi {
        return OppgaveKopi(
            id = oppgave.id,
            versjon = oppgave.versjon,
            journalpostId = oppgave.journalpostId,
            saksreferanse = oppgave.saksreferanse,
            mappeId = oppgave.mappeId,
            status = Status.valueOf(oppgave.status.name),
            tildeltEnhetsnr = oppgave.tildeltEnhetsnr,
            opprettetAvEnhetsnr = oppgave.opprettetAvEnhetsnr,
            endretAvEnhetsnr = oppgave.endretAvEnhetsnr,
            tema = oppgave.tema,
            temagruppe = oppgave.temagruppe,
            behandlingstema = oppgave.behandlingstema,
            oppgavetype = oppgave.oppgavetype,
            behandlingstype = oppgave.behandlingstype,
            prioritet = Prioritet.valueOf(oppgave.prioritet.name),
            tilordnetRessurs = oppgave.tilordnetRessurs,
            beskrivelse = oppgave.beskrivelse,
            fristFerdigstillelse = oppgave.fristFerdigstillelse,
            aktivDato = oppgave.aktivDato,
            opprettetAv = oppgave.opprettetAv,
            endretAv = oppgave.endretAv,
            opprettetTidspunkt = oppgave.opprettetTidspunkt,
            endretTidspunkt = oppgave.endretTidspunkt,
            ferdigstiltTidspunkt = oppgave.ferdigstiltTidspunkt,
            behandlesAvApplikasjon = oppgave.behandlesAvApplikasjon,
            journalpostkilde = oppgave.journalpostkilde,
            ident = Ident(
                id = oppgave.ident.id,
                identType = IdentType.valueOf(oppgave.ident.identType.name),
                verdi = oppgave.ident.verdi,
                folkeregisterident = oppgave.ident.folkeregisterident,
                registrertDato = null
            ),
            metadata = oppgave.metadata?.map { (k, v) ->
                MetadataNoekkel.valueOf(k.name) to v
            }?.toMap() ?: emptyMap()
        )
    }
}
