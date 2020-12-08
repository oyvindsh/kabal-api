package no.nav.klage.oppgave.repositories

import no.nav.klage.oppgave.domain.oppgavekopi.Ident
import no.nav.klage.oppgave.domain.oppgavekopi.MetadataNoekkel
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import org.springframework.jdbc.core.simple.SimpleJdbcInsert
import org.springframework.jdbc.support.incrementer.PostgresSequenceMaxValueIncrementer
import javax.sql.DataSource

class OppgaveKopiRepository(
    val dataSource: DataSource,
    val identIdIncrementer: PostgresSequenceMaxValueIncrementer,
    val metadataIdIncrementer: PostgresSequenceMaxValueIncrementer,
    val versjonMetadataIdIncrementer: PostgresSequenceMaxValueIncrementer
) {

    private fun identParameterMap(ident: Ident, identId: Long) =
        mapOf(
            "id" to identId,
            "TYPE" to ident.identType.name,
            "verdi" to ident.verdi,
            "folkeregisterident" to ident.folkeregisterident,
            "registrert_dato" to ident.registrertDato
        )

    private fun metadataParameterMap(oppgaveKopi: OppgaveKopi, noekkel: MetadataNoekkel, verdi: String) =
        mapOf(
            "id" to metadataIdIncrementer.nextLongValue(),
            "oppgave_id" to oppgaveKopi.id,
            "nokkel" to noekkel.name,
            "verdi" to verdi,
        )

    private fun versjonMetadataParameterMap(oppgaveKopi: OppgaveKopi, noekkel: MetadataNoekkel, verdi: String) =
        mapOf(
            "id" to versjonMetadataIdIncrementer.nextLongValue(),
            "oppgave_id" to oppgaveKopi.id,
            "oppgave_versjon" to oppgaveKopi.versjon,
            "nokkel" to noekkel.name,
            "verdi" to verdi
        )

    private fun oppgaveParameterMap(oppgaveKopi: OppgaveKopi, identId: Long?) =
        mapOf(
            "id" to oppgaveKopi.id,
            "versjon" to oppgaveKopi.versjon,
            "journalpostid" to oppgaveKopi.journalpostId,
            "saksreferanse" to oppgaveKopi.saksreferanse,
            "mappe_id" to oppgaveKopi.mappeId,
            "status_id" to oppgaveKopi.status.statusId,
            "tildelt_enhetsnr" to oppgaveKopi.tildeltEnhetsnr,
            "opprettet_av_enhetsnr" to oppgaveKopi.opprettetAvEnhetsnr,
            "endret_av_enhetsnr" to oppgaveKopi.endretAvEnhetsnr,
            "tema" to oppgaveKopi.tema,
            "temagruppe" to oppgaveKopi.temagruppe,
            "behandlingstema" to oppgaveKopi.behandlingstema,
            "oppgavetype" to oppgaveKopi.oppgavetype,
            "behandlingstype" to oppgaveKopi.behandlingstype,
            "prioritet" to oppgaveKopi.prioritet.name,
            "tilordnet_ressurs" to oppgaveKopi.tilordnetRessurs,
            "beskrivelse" to oppgaveKopi.beskrivelse,
            "frist_ferdigstillelse" to oppgaveKopi.fristFerdigstillelse,
            "aktiv_dato" to oppgaveKopi.aktivDato,
            "opprettet_av" to oppgaveKopi.opprettetAv,
            "endret_av" to oppgaveKopi.endretAv,
            "opprettet_tidspunkt" to oppgaveKopi.opprettetTidspunkt,
            "endret_tidspunkt" to oppgaveKopi.endretTidspunkt,
            "ferdigstilt_tidspunkt" to oppgaveKopi.ferdigstiltTidspunkt,
            "behandles_av_applikasjon" to oppgaveKopi.behandlesAvApplikasjon,
            "journalpostkilde" to oppgaveKopi.journalpostkilde,
            "ident_id" to identId
        )

    val oppgaveJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("oppgave")
    val oppgaveVersjonJdbcInsert =
        SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("oppgaveversjon")
    val metadataJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("metadata")
    val versjonMetadataJdbcInsert =
        SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("versjonmetadata")
    val identJdbcInsert = SimpleJdbcInsert(dataSource).withSchemaName("oppgave").withTableName("ident")

    private fun lagreIdentAndReturnId(ident: Ident?): Long? {
        return ident?.let {
            val identId = identIdIncrementer.nextLongValue()
            val identParameterMap = identParameterMap(it, identId)
            identJdbcInsert.execute(identParameterMap)
            identId
        }
    }

    fun lagreOppgaveKopi(oppgaveKopi: OppgaveKopi) {
        val identId = lagreIdentAndReturnId(oppgaveKopi.ident)
        oppgaveJdbcInsert.execute(oppgaveParameterMap(oppgaveKopi, identId))
        oppgaveKopi.metadata?.forEach { metadataNoekkel, verdi ->
            metadataJdbcInsert.execute(metadataParameterMap(oppgaveKopi, metadataNoekkel, verdi))
        }
        oppgaveVersjonJdbcInsert.execute(oppgaveParameterMap(oppgaveKopi, identId))
        oppgaveKopi.metadata?.forEach { metadataNoekkel, verdi ->
            versjonMetadataJdbcInsert.execute(versjonMetadataParameterMap(oppgaveKopi, metadataNoekkel, verdi))
        }
    }
}