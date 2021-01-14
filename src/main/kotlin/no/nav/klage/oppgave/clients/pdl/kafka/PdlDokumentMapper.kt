package no.nav.klage.oppgave.clients.pdl.kafka

import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger

object PdlDokumentMapper {

    private val logger = getLogger(javaClass.enclosingClass)
    private val secureLogger = getSecureLogger()


    fun mapToPerson(pdlDokument: PdlDokument): Person? {
        val ikkeHistoriskeFnr =
            pdlDokument.hentIdenter.identer.filter { it.gruppe == IdentGruppeDto.FOLKEREGISTERIDENT }
                .filter { !it.historisk }
        if (ikkeHistoriskeFnr.count() == 0 || ikkeHistoriskeFnr.count() > 1) {
            logger.debug("Fant ${ikkeHistoriskeFnr.count()} potensielle fnr")
        }
        val foedslsnr = ikkeHistoriskeFnr.firstOrNull()

        val ikkeHistoriskeNavn = pdlDokument.hentPerson.navn.filter { !it.metadata.historisk }
        if (ikkeHistoriskeNavn.count() == 0 || ikkeHistoriskeNavn.count() > 1) {
            logger.debug("Fant ${ikkeHistoriskeNavn.count()} potensielle navn")
        }
        val navn = ikkeHistoriskeNavn.firstOrNull()

        val ikkeHistoriskeAdressebeskyttelser =
            pdlDokument.hentPerson.adressebeskyttelse.filter { !it.metadata.historisk }
        if (ikkeHistoriskeAdressebeskyttelser.count() > 1) {
            logger.debug("Fant ${ikkeHistoriskeAdressebeskyttelser.count()} potensielle adressebeskyttelser")
        }
        val adressebeskyttelse = ikkeHistoriskeAdressebeskyttelser.firstOrNull()
        return if (foedslsnr == null || navn == null) {
            secureLogger.warn("Foedselsnr or navn is missing, cannot proceed with $this")
            null
        } else {
            Person(
                foedslsnr.ident,
                navn.fornavn,
                navn.mellomnavn,
                navn.etternavn,
                "${navn.fornavn} ${navn.etternavn}",
                adressebeskyttelse?.gradering?.mapToPerson()
            )
        }
    }

    private fun GraderingDto.mapToPerson(): Beskyttelsesbehov? =
        when (this) {
            GraderingDto.FORTROLIG -> Beskyttelsesbehov.FORTROLIG
            GraderingDto.STRENGT_FORTROLIG -> Beskyttelsesbehov.STRENGT_FORTROLIG
            GraderingDto.STRENGT_FORTROLIG_UTLAND -> Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND
            else -> null
        }
}