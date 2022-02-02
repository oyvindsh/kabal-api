package no.nav.klage.dokument.domain

import java.util.*

data class SmartEditorDokument(val id: UUID, val mellomlagretDokument: MellomlagretDokument) :
    IMellomlagretDokument by mellomlagretDokument