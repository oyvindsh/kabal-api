package no.nav.klage.dokument.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class MoveNodeSlateOperationTest {

    @Test
    fun `path equals new path - move node`() {
        val input = """
        {
            "children":    
            [
                {
                  "children": [
                    {
                      "text": "1"
                    }
                  ]
                },
                {
                  "children": [
                    {
                      "text": "2"
                    }
                  ]
                }
            ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "move_node", path = "[0]", newPath = "[1]",
        )

        val expected = """
        {
            "children":    
            [
                {
                  "children": [
                    {
                      "text": "2"
                    }
                  ]
                },
                {
                  "children": [
                    {
                      "text": "1"
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

    @Test
    fun `path-not-equals-new-path - move node`() {
        val input = """
        {
            "children":    
            [
                {
                  "children": [
                    {
                      "text": "1"
                    }
                  ]
                },
                {
                  "children": [
                    {
                      "text": "2"
                    }
                  ]
                }
            ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "move_node", path = "[0]", newPath = "[0]",
        )

        val expected = """
        {
            "children":    
            [
                {
                  "children": [
                    {
                      "text": "1"
                    }
                  ]
                },
                {
                  "children": [
                    {
                      "text": "2"
                    }
                  ]
                }
            ]
        }
            """.trimIndent()

        val applied = apply(input, operation)

        val inputResultAsJsonNode = jacksonObjectMapper().readTree(applied)
        val expectedAsJsonNode = jacksonObjectMapper().readTree(expected)

        assertThat(inputResultAsJsonNode).isEqualTo(expectedAsJsonNode)
    }

}