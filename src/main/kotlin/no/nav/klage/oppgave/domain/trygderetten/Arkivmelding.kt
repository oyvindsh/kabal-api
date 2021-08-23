package no.nav.klage.oppgave.domain.trygderetten

import org.redundent.kotlin.xml.xml

class Arkivmelding(val id: Long, val firstName: String, val lastName: String, val phone: String) {

    fun toXml(): String {
        return xml("people") {
            xmlns = "http://example.com/people"
            "person" {
                attribute("id", id)
                "firstName" {
                    -firstName
                }
                "lastName" {
                    -lastName
                }
                "phone" {
                    -phone
                }
            }
        }.toString()
    }
}