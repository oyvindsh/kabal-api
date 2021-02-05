package no.nav.klage.oppgave.service

import no.nav.klage.oppgave.domain.klage.Hjemmel
import no.nav.klage.oppgave.domain.oppgavekopi.MetadataNoekkel
import no.nav.klage.oppgave.domain.oppgavekopi.OppgaveKopi
import org.springframework.stereotype.Service

@Service
class HjemmelService {
    private val hjemmelRegex = """(\d{1,2}-\d{1,2})+""".toRegex()

    fun getHjemmelFromOppgaveKopi(oppgaveKopi: OppgaveKopi): MutableList<Hjemmel> {
        val metadataHjemmel = oppgaveKopi.metadata.find {
            it.noekkel == MetadataNoekkel.HJEMMEL && it.verdi.matchesHjemmelRegex()
        }
        if (metadataHjemmel != null) {
            return mutableListOf(generateHjemmelFromText(metadataHjemmel.verdi))
        }
        val hjemler = hjemmelRegex.findAll(oppgaveKopi.beskrivelse ?: "").collect()
        if (hjemler.isNotEmpty()) {
            return mutableListOf(generateHjemmelFromText(hjemler[0]))
        }
        return mutableListOf()
    }

    private fun generateHjemmelFromText(hjemmelText: String): Hjemmel {
        val parts = hjemmelText.split("-")
        return Hjemmel(
            original = hjemmelText,
            kapittel = parts[0].toInt(),
            paragraf = parts[1].toInt()
        )
    }

    private fun Sequence<MatchResult>.collect(): List<String> {
        val list = mutableListOf<String>()
        this.iterator().forEachRemaining {
            val hjemmel = it.value.replace("§", "").trim()
            list.add(hjemmel)
        }
        return list
    }

    private fun String.matchesHjemmelRegex() = hjemmelRegex.find(this) != null
}
