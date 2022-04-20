package no.nav.klage.dokument.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class RemoveTextSlateOperationTest {

    @Test
    fun `remove text`() {
        val input = """
        {
            "children":
            [
              {
                "children": [
                  {
                    "text": "word"
                  }
                ]
              }
            ]
        }
        """.trimIndent()

        val operation = SlateOperation(
            type = "remove_text", path = "[0, 0]", text = "or", offset = 1,
        )

        val expected = """
        {
            "children":
            [
              {
                "children": [
                  {
                    "text": "wd"
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