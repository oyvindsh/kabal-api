package no.nav.klage.dokument

import no.nav.klage.oppgave.service.migrateTables
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MigrateSmartDocumentTablesTest {

    @Test
    fun `migrate tables works`() {
        assertThat(migrateTables(
            fromJsonString = initialJsonString,
            secureLogger = null
        )).isEqualToIgnoringWhitespace(expectedJsonString)
    }

    @Test
    fun `migrate tables is idempotent`() {
        assertThat(migrateTables(
            fromJsonString = expectedJsonString,
            secureLogger = null
        )).isEqualToIgnoringWhitespace(expectedJsonString)
    }

}

val initialJsonString = """
    {
        "content": [
            {
                "type": "h1",
                "align": "left",
                "children": [
                    {
                        "text": "Regelverk"
                    }
                ]
            },
            {
                "type": "table",
                "children": [
                    {
                        "type": "tbody",
                        "children": [
                            {
                                "type": "tr",
                                "children": [
                                    {
                                        "type": "td",
                                        "children": [
                                            {
                                                "type": "p",
                                                "align": "left",
                                                "children": [
                                                    {
                                                        "text": "a"
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "type": "td",
                                        "children": [
                                            {
                                                "type": "p",
                                                "align": "left",
                                                "children": [
                                                    {
                                                        "text": "b"
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "type": "td",
                                        "children": [
                                            {
                                                "type": "p",
                                                "children": [
                                                    {
                                                        "text": "c"
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "type": "td",
                                        "children": [
                                            {
                                                "type": "p",
                                                "children": [
                                                    {
                                                        "text": "d"
                                                    }
                                                ]
                                            }
                                        ]
                                    },
                                    {
                                        "type": "td",
                                        "children": [
                                            {
                                                "type": "p",
                                                "children": [
                                                    {
                                                        "text": "e"
                                                    }
                                                ]
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ],
                "colSizes": [
                    0,
                    0,
                    0,
                    0,
                    0
                ]
            },
            {
                "type": "p",
                "children": [
                    {
                        "text": "Andre "
                    },
                    {
                        "text": "regler",
                        "bold": true
                    }
                ]
            }
        ]
    }
""".trimIndent()


val expectedJsonString = """
    {
        "content": [
            {
                "type": "h1",
                "align": "left",
                "children": [
                    {
                        "text": "Regelverk"
                    }
                ]
            },
            {
                "type": "table",
                "children": [
                    {
                        "type": "tr",
                        "children": [
                            {
                                "type": "td",
                                "children": [
                                    {
                                        "type": "p",
                                        "align": "left",
                                        "children": [
                                            {
                                                "text": "a"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "type": "td",
                                "children": [
                                    {
                                        "type": "p",
                                        "align": "left",
                                        "children": [
                                            {
                                                "text": "b"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "type": "td",
                                "children": [
                                    {
                                        "type": "p",
                                        "children": [
                                            {
                                                "text": "c"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "type": "td",
                                "children": [
                                    {
                                        "type": "p",
                                        "children": [
                                            {
                                                "text": "d"
                                            }
                                        ]
                                    }
                                ]
                            },
                            {
                                "type": "td",
                                "children": [
                                    {
                                        "type": "p",
                                        "children": [
                                            {
                                                "text": "e"
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    }
                ],
                "colSizes": [
                    0,
                    0,
                    0,
                    0,
                    0
                ]
            },
            {
                "type": "p",
                "children": [
                    {
                        "text": "Andre "
                    },
                    {
                        "text": "regler",
                        "bold": true
                    }
                ]
            }
        ]
    }
""".trimIndent()
