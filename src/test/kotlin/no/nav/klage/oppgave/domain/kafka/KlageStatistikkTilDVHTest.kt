package no.nav.klage.oppgave.domain.kafka

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig
import com.kjetland.jackson.jsonSchema.JsonSchemaDraft
import com.kjetland.jackson.jsonSchema.JsonSchemaGenerator

internal class KlageStatistikkTilDVHTest {

    //@Test
    fun createJsonSchema() {
        val objectMapper = jacksonObjectMapper().registerModule(JavaTimeModule())

        val config = JsonSchemaConfig.vanillaJsonSchemaDraft4().withJsonSchemaDraft(JsonSchemaDraft.DRAFT_07)

        val schemaGen = JsonSchemaGenerator(objectMapper, config)
        val jsonSchema = schemaGen.generateJsonSchema(KlageStatistikkTilDVH::class.java)

        println(objectMapper.writeValueAsString(jsonSchema))
    }

}