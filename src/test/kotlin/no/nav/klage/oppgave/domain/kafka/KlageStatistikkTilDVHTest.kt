package no.nav.klage.oppgave.domain.kafka

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

internal class KlageStatistikkTilDVHTest {

//    @Test
    fun createJsonSchema() {
        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        val schemaGen = JsonSchemaGenerator(objectMapper)
        val jsonSchema = schemaGen.generateSchema(KlageStatistikkTilDVH::class.java)

        println(objectMapper.writeValueAsString(jsonSchema))
    }

}