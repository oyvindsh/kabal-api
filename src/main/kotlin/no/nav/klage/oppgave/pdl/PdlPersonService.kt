package no.nav.klage.oppgave.pdl

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class PdlPersonService {

    private val pdlPersonMap: ConcurrentMap<String, PdlPerson> = ConcurrentHashMap()

    fun erFortrolig(foedselsnr: String): Boolean =
        pdlPersonMap.getValue(foedselsnr).beskyttelsesbehov == Beskyttelsesbehov.FORTROLIG

    fun erStrengtFortrolig(foedselsnr: String): Boolean =
        pdlPersonMap.getValue(foedselsnr).beskyttelsesbehov == Beskyttelsesbehov.STRENGT_FORTROLIG || pdlPersonMap.getValue(
            foedselsnr
        ).beskyttelsesbehov == Beskyttelsesbehov.STRENGT_FORTROLIG_UTLAND

    fun navn(foedselsnr: String): String =
        "${pdlPersonMap.getValue(foedselsnr).fornavn} ${pdlPersonMap.getValue(foedselsnr).etternavn}"

    fun oppdaterPdlPerson(pdlPerson: PdlPerson) {
        pdlPersonMap[pdlPerson.foedselsnr] = pdlPerson
    }
}