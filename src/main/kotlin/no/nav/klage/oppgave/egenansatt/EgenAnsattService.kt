package no.nav.klage.oppgave.egenansatt

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class EgenAnsattService {

    private val egenAnsattMap: ConcurrentMap<String, EgenAnsatt> = ConcurrentHashMap()

    fun erEgenAnsatt(fodselsnr: String): Boolean =
        egenAnsattMap[fodselsnr]?.erGyldig() ?: false

    fun oppdaterEgenAnsatt(fodselsnr: String, egenAnsatt: EgenAnsatt) {
        egenAnsattMap[fodselsnr] = egenAnsatt
    }
}