package no.nav.klage.oppgave.clients.pdl.kafka

import no.nav.klage.oppgave.clients.pdl.Person
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Service
class PersonCacheService {

    private val personMap: ConcurrentMap<String, Person> = ConcurrentHashMap()

    fun isCached(foedselsnr: String): Boolean = personMap.containsKey(foedselsnr)

    fun getPerson(foedselsnr: String) = personMap.getValue(foedselsnr)

    fun oppdaterPersonCache(person: Person) {
        personMap[person.foedselsnr] = person
    }
}