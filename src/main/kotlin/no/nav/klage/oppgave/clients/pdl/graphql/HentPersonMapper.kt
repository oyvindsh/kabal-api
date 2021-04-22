package no.nav.klage.oppgave.clients.pdl.graphql

import no.nav.klage.oppgave.clients.pdl.Beskyttelsesbehov
import no.nav.klage.oppgave.clients.pdl.Person
import no.nav.klage.oppgave.util.getLogger
import no.nav.klage.oppgave.util.getSecureLogger
import org.springframework.stereotype.Component

@Component
class HentPersonMapper {

    companion object {
        @Suppress("JAVA_CLASS_ON_COMPANION")
        private val logger = getLogger(javaClass.enclosingClass)
        private val secureLogger = getSecureLogger()
    }

    fun mapToPerson(fnr: String, pdlPerson: PdlPerson): Person {

        secureLogger.debug("pdl returned {}", pdlPerson)
        return Person(
            foedselsnr = fnr,
            fornavn = pdlPerson.navn.firstOrNull()?.fornavn,
            mellomnavn = pdlPerson.navn.firstOrNull()?.mellomnavn,
            etternavn = pdlPerson.navn.firstOrNull()?.etternavn,
            navn = sammensattNavn(pdlPerson.navn.firstOrNull()),
            beskyttelsesbehov = pdlPerson.adressebeskyttelse.firstOrNull()?.gradering?.mapToBeskyttelsesbehov(),
            kjoenn = pdlPerson.kjoenn.firstOrNull()?.kjoenn?.name
        )
    }

    fun mapToPersoner(people: List<HentPersonBolkResult>): List<Person> {

        logger.debug("pdl returned {} people", people.size)
        secureLogger.debug("pdl returned {}", people)

        return people.map {
            Person(
                foedselsnr = it.ident,
                fornavn = it.person?.navn?.firstOrNull()?.fornavn,
                mellomnavn = it.person?.navn?.firstOrNull()?.mellomnavn,
                etternavn = it.person?.navn?.firstOrNull()?.etternavn,
                navn = sammensattNavn(it.person?.navn?.firstOrNull()),
                beskyttelsesbehov = it.person?.adressebeskyttelse?.firstOrNull()?.gradering?.mapToBeskyttelsesbehov(),
                kjoenn = it.person?.kjoenn?.firstOrNull()?.kjoenn?.name
            )
        }
    }

    private fun sammensattNavn(navn: PdlPerson.Navn?): String? =
        navn?.let { "${it.fornavn} ${it.etternavn}" }

    fun PdlPerson.Adressebeskyttelse.GraderingType.mapToBeskyttelsesbehov(): Beskyttelsesbehov? =
        when (this) {
            PdlPerson.Adressebeskyttelse.GraderingType.FORTROLIG -> Beskyttelsesbehov.FORTROLIG
            PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG -> Beskyttelsesbehov.STRENGT_FORTROLIG
            PdlPerson.Adressebeskyttelse.GraderingType.STRENGT_FORTROLIG_UTLAND -> Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND
            else -> null
        }

}
