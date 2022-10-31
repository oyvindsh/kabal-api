package no.nav.klage.dokument.exceptions

import no.nav.klage.dokument.api.view.DocumentValidationResponse

class DokumentValidationException(msg: String) : RuntimeException(msg)

class JsonToPdfValidationException(msg: String, val errors: List<DocumentValidationResponse>) : RuntimeException(msg)