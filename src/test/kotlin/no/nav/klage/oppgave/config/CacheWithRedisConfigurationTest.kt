package no.nav.klage.oppgave.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.klage.oppgave.domain.Tilganger
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest

@JsonTest
@Disabled
internal class CacheWithRedisConfigurationTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun skalSerialisereOgDeserialisereMedJackson() {
        val tilganger = Tilganger(emptyList())
        val string = objectMapper.writeValueAsString(tilganger)
        val roundtrippaTilganger: Tilganger = objectMapper.readValue(string)
    }
}