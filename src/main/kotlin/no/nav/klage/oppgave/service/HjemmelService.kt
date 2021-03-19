package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Hjemmel
import org.springframework.stereotype.Service

@Service
class HjemmelService {
    private val hjemmelRegex = """(\d{1,2}-\d{1,2})+""".toRegex()

    fun generateHjemmelFromText(hjemmelText: String): Hjemmel {
        val parts = hjemmelText.split("-")
        return Hjemmel(
            original = hjemmelText,
            kapittel = parts[0].toInt(),
            paragraf = parts[1].toInt()
        )
    }
}
