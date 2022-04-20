package no.nav.klage.dokument.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RemoveNodeSlateOperationTest {

    @Test
    fun `remove node`() {
        val input = """
        {
            "children":
            [
              {
                "children": [
                  {
                    "text": "element"
                  }
                ]
              },
              {
                "children": [
                  {
                    "children": [
                      {
                        "text": "nested element"
                      }
                    ]
                  }
                ]
              }
            ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "remove_node", path = "[1, 0, 0]",
        )

        val expected = """
        {
            "children": [
              {
                "children": [
                  {
                    "text": "element"
                  }
                ]
              },
              {
                "children": [
                  {
                    "children": []
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
    fun `remove node 2`() {
        val input = """
        {
            "children":
            [
              {
                "children": [
                  {
                    "text": "element"
                  }
                ]
              },
              {
                "children": [
                  {
                    "text": "another element"
                  }
                ]
              }
            ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "remove_node", path = "[1, 0]",
        )

        val expected = """
        {
            "children":
             [
               {
                 "children": [
                   {
                     "text": "element"
                   }
                 ]
               },
               {
                 "children": []
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