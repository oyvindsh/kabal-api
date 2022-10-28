package no.nav.klage.dokument.exceptions

class DokumentValidationException(msg: String) : RuntimeException(msg)

class JsonDokumentValidationException(msg: String) : RuntimeException(msg)