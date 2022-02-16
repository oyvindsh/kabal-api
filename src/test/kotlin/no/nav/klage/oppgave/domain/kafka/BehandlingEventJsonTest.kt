package no.nav.klage.oppgave.domain.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

internal class BehandlingEventJsonTest{

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    @Test
    fun `event genererer riktig json`() {

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)


        val event = BehandlingEvent(
            eventId = UUID.randomUUID(),
        kildeReferanse = "kildeReferanse",
            kilde = "kilde",
            kabalReferanse = "kabalReferanse",
            type = BehandlingEventType.ANKEBEHANDLING_OPPRETTET,
            detaljer = BehandlingDetaljer(
                ankebehandlingOpprettet = AnkebehandlingOpprettetDetaljer(
                    mottattKlageinstans = LocalDateTime.of(2022, 7, 20, 10, 30)
                )
            )
        )
        val jsonString = objectMapper.writeValueAsString(event)
        assertThat(jsonString).contains("\"mottattKlageinstans\":\"2022-07-20T10:30:00\"")
    }
}