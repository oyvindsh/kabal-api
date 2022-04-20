package no.nav.klage.dokument.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class SplitNodeSlateOperationTest {

    @Test
    @Disabled
    fun `split node`() {
        val input = """
        {
            "children":    
            [
              {
                "type": "paragraph",
                "textAlign": "text-align-left",
                "children": [
                  {
                    "text": "textsplit"
                  }
                ]
              }
            ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "split_node",
            path = "[0, 0]",
            properties = mapOf(),
            position = 4
        )

        val expected = """
        {
            "children":    
            [
              {
                "type": "paragraph",
                "textAlign": "text-align-left",
                "children": [
                  {
                    "text": "text"
                  },
                  {
                    "text": "split"
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