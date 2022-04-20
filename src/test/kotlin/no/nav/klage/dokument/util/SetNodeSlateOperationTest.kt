package no.nav.klage.dokument.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class SetNodeSlateOperationTest {

    @Test
    fun `set node`() {
        val input = """
        {
            "children":    
            [
              {
                "type": "paragraph",
                "children": [
                  {
                    "text": "paragraph"
                  }
                ]
              }
        ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "set_node",
            path = "[0]",
            properties = mapOf("type" to "paragraph"),
            newProperties = mapOf("type" to "heading-one")
        )

        val expected = """
        {
            "children":    
            [
              {
                "type": "heading-one",
                "children": [
                  {
                    "text": "paragraph"
                  }
                ]
              }
            ]
        }
            """.trimIndent()

        val applied = apply(input, operation)

        //For comparison
        val inputResultAsJsonNode = jacksonObjectMapper().readTree(applied)
        val expectedAsJsonNode = jacksonObjectMapper().readTree(expected)

        assertThat(inputResultAsJsonNode).isEqualTo(expectedAsJsonNode)
    }

}