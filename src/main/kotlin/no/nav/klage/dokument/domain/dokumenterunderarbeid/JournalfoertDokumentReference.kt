package no.nav.klage.dokument.domain.dokumenterunderarbeid

import jakarta.persistence.Embeddable

@Embeddable
data class JournalfoertDokumentReference(
    val journalpostId: String,
    val dokumentInfoId: String,
)